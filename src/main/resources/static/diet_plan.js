// ── Data Layer ────────────────────────────────────────────────
const STORAGE_KEY = 'fitconnect_diet_plans';
const GOAL_KEY    = 'fitconnect_calorie_goal';
const DAYS        = ['Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday'];
const TYPE_ORDER  = ['breakfast','lunch','snack','dinner'];

let meals          = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
let dailyGoal      = parseInt(localStorage.getItem(GOAL_KEY) || '2000', 10);
let activeFilter   = 'all';
let weekOffset     = 0;
let deleteTarget   = null;
let reminderEnabled = false;
let reminderTimers  = [];

try { reminderEnabled = Notification.permission === 'granted'; } catch(e) {}

// ── Init ──────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    const goalInput = document.getElementById('goalInput');
    if (goalInput) goalInput.value = dailyGoal;

    updateWeekLabel();
    renderAll();
    updateReminderUI();
    scheduleReminders();

    // Initialise first filter tab style
    const firstTab = document.querySelector('.filter-tab');
    if (firstTab) applyActiveTab(firstTab);
});

// ── Persist ───────────────────────────────────────────────────
function persist() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(meals));
}

// ── Week Helpers ──────────────────────────────────────────────
function getWeekDates(offset) {
    offset = offset || 0;
    const now    = new Date();
    const day    = now.getDay();
    const monday = new Date(now);
    monday.setDate(now.getDate() - (day === 0 ? 6 : day - 1) + offset * 7);
    return DAYS.map(function(_, i) {
        const d = new Date(monday);
        d.setDate(monday.getDate() + i);
        return d;
    });
}

function isToday(date) {
    const t = new Date();
    return date.getDate() === t.getDate()
        && date.getMonth() === t.getMonth()
        && date.getFullYear() === t.getFullYear();
}

function updateWeekLabel() {
    const dates = getWeekDates(weekOffset);
    const fmt   = function(d) { return d.toLocaleDateString('en-US', {month:'short', day:'numeric'}); };
    const el    = document.getElementById('weekLabel');
    if (el) el.textContent = 'Week of ' + fmt(dates[0]) + ' \u2013 ' + fmt(dates[6]);
}

function changeWeek(dir) {
    weekOffset += dir;
    updateWeekLabel();
    renderWeekGrid();
}

// ── Render: All ───────────────────────────────────────────────
function renderAll() {
    renderWeekGrid();
    renderTable();
    renderSummary();
    renderReminderList();
}

// ── Render: Weekly Grid ───────────────────────────────────────
function renderWeekGrid() {
    const grid  = document.getElementById('weekGrid');
    if (!grid) return;
    const dates = getWeekDates(weekOffset);
    grid.innerHTML = '';

    dates.forEach(function(date, i) {
        const dayName  = DAYS[i];
        const dayMeals = meals
            .filter(function(m) { return m.day === dayName && (activeFilter === 'all' || m.type === activeFilter); })
            .sort(function(a, b) { return TYPE_ORDER.indexOf(a.type) - TYPE_ORDER.indexOf(b.type); });
        const dayCal  = dayMeals.reduce(function(s, m) { return s + (parseInt(m.calories) || 0); }, 0);
        const today   = isToday(date);

        const col = document.createElement('div');
        col.className = 'glass-effect rounded-xl p-3' + (today ? ' day-col-active ring-1 ring-purple-500/50' : '');

        const dateStr   = date.toLocaleDateString('en-US', {month:'short', day:'numeric'});
        const dayLabel  = today
            ? '<span class="text-[9px] px-1.5 py-0.5 bg-purple-600/50 rounded-full text-purple-300">Today</span>'
            : '';
        const nameClass = today ? 'text-purple-300' : 'text-gray-300';

        let chipsHtml = dayMeals.length === 0
            ? '<p class="text-[11px] text-gray-600 text-center py-2">No meals</p>'
            : dayMeals.map(function(m) { return mealChip(m); }).join('');

        col.innerHTML =
            '<div class="flex items-center justify-between mb-2">' +
                '<div>' +
                    '<p class="text-xs font-bold ' + nameClass + '">' + dayName.slice(0,3).toUpperCase() + '</p>' +
                    '<p class="text-[10px] text-gray-500">' + dateStr + '</p>' +
                '</div>' +
                '<div class="text-right">' +
                    '<p class="text-[10px] text-gray-400">' + dayCal + ' kcal</p>' +
                    dayLabel +
                '</div>' +
            '</div>' +
            '<div class="space-y-1" id="day-' + dayName + '">' + chipsHtml + '</div>' +
            '<button onclick="openAddModalForDay(\'' + dayName + '\')" ' +
                'class="mt-2 w-full text-[10px] text-gray-500 hover:text-gray-300 border border-dashed border-white/10 hover:border-white/30 rounded-lg py-1 transition-all">' +
                '+ add' +
            '</button>';

        grid.appendChild(col);
    });
}

function mealChip(m) {
    const typeClass = {breakfast:'badge-breakfast',lunch:'badge-lunch',dinner:'badge-dinner',snack:'badge-snack'}[m.type] || '';
    const bell      = m.reminder ? '\uD83D\uDD14 ' : '';
    const notesAttr = m.notes ? m.notes.replace(/"/g, '&quot;') : '';
    return '<div class="rounded-lg p-1.5 mb-1 cursor-pointer hover:opacity-80 transition-all ' + typeClass + '" ' +
               'onclick="openEditModal(\'' + m.id + '\')" title="' + notesAttr + '">' +
               '<p class="text-[10px] font-semibold text-white truncate">' + bell + escHtml(m.name) + '</p>' +
               '<p class="text-[9px] text-white/70">' + (m.time || '') + ' ' + (m.calories ? m.calories + 'kcal' : '') + '</p>' +
           '</div>';
}

// ── Render: Table ─────────────────────────────────────────────
function renderTable() {
    const searchEl = document.getElementById('searchInput');
    const q        = searchEl ? searchEl.value.toLowerCase() : '';
    const tbody    = document.getElementById('mealTableBody');
    const empty    = document.getElementById('emptyMsg');
    if (!tbody) return;

    const filtered = meals.filter(function(m) {
        return (activeFilter === 'all' || m.type === activeFilter) &&
               (!q || m.name.toLowerCase().includes(q) || m.day.toLowerCase().includes(q));
    }).sort(function(a, b) {
        const di = DAYS.indexOf(a.day) - DAYS.indexOf(b.day);
        return di !== 0 ? di : TYPE_ORDER.indexOf(a.type) - TYPE_ORDER.indexOf(b.type);
    });

    if (filtered.length === 0) {
        tbody.innerHTML = '';
        if (empty) empty.classList.remove('hidden');
        return;
    }
    if (empty) empty.classList.add('hidden');

    tbody.innerHTML = filtered.map(function(m) {
        const notesHtml = m.notes
            ? '<p class="text-xs text-gray-500 mt-0.5 truncate max-w-xs">' + escHtml(m.notes) + '</p>'
            : '';
        const bellIcon = m.reminder
            ? '<i class="fas fa-bell text-yellow-400 text-xs" title="Reminder set"></i>'
            : '';
        return '<tr class="border-b border-white/5 hover:bg-white/5 transition-all group">' +
            '<td class="py-3 pr-4">' +
                '<p class="font-medium text-white/90">' + escHtml(m.name) + '</p>' +
                notesHtml +
            '</td>' +
            '<td class="py-3 pr-4">' +
                '<span class="inline-block px-2 py-0.5 rounded-full text-[11px] font-semibold ' + badgeClass(m.type) + ' text-white capitalize">' + m.type + '</span>' +
            '</td>' +
            '<td class="py-3 pr-4 text-gray-300 text-xs">' + m.day + '</td>' +
            '<td class="py-3 pr-4 text-gray-300 text-xs">' + (m.time || '\u2014') + '</td>' +
            '<td class="py-3 pr-4 text-gray-300 text-xs">' + (m.calories ? m.calories + ' kcal' : '\u2014') + '</td>' +
            '<td class="py-3 pr-4 text-gray-300 text-xs">' + (m.protein ? m.protein + 'g' : '\u2014') + '</td>' +
            '<td class="py-3">' +
                '<div class="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-all">' +
                    '<button onclick="openEditModal(\'' + m.id + '\')" class="text-blue-400 hover:text-blue-300 transition-colors" title="Edit"><i class="fas fa-edit"></i></button>' +
                    '<button onclick="openDeleteModal(\'' + m.id + '\')" class="text-red-400 hover:text-red-300 transition-colors" title="Delete"><i class="fas fa-trash-alt"></i></button>' +
                    bellIcon +
                '</div>' +
            '</td>' +
        '</tr>';
    }).join('');
}

function badgeClass(type) {
    return {breakfast:'badge-breakfast',lunch:'badge-lunch',dinner:'badge-dinner',snack:'badge-snack'}[type] || 'bg-gray-600';
}

// ── Render: Summary Cards ─────────────────────────────────────
function renderSummary() {
    const todayIdx   = (new Date().getDay() + 6) % 7; // Mon=0
    const todayName  = DAYS[todayIdx];
    const todayMeals = meals.filter(function(m) { return m.day === todayName; });
    const todayCal   = todayMeals.reduce(function(s, m) { return s + (parseInt(m.calories) || 0); }, 0);
    const pct        = Math.min(dailyGoal > 0 ? (todayCal / dailyGoal) * 100 : 0, 100).toFixed(1);

    setText('todayCalories', todayCal);
    setText('dailyGoal', dailyGoal);
    setText('totalMeals', meals.length);

    const bar = document.getElementById('calorieBar');
    if (bar) bar.style.width = pct + '%';

    const days         = Array.from(new Set(meals.map(function(m) { return m.day; })));
    const totalProtein = meals.reduce(function(s, m) { return s + (parseInt(m.protein) || 0); }, 0);
    const avg          = days.length ? Math.round(totalProtein / days.length) : 0;
    setText('avgProtein', avg + 'g');
    setText('daysTracked', days.length);
}

// ── Filter Tabs ───────────────────────────────────────────────
function setFilter(f, btn) {
    activeFilter = f;
    document.querySelectorAll('.filter-tab').forEach(function(t) {
        t.classList.remove('bg-gradient-to-r','from-purple-600','to-blue-600','text-white');
        t.classList.add('glass-effect','text-gray-300');
    });
    applyActiveTab(btn);
    renderAll();
}

function applyActiveTab(btn) {
    btn.classList.add('bg-gradient-to-r','from-purple-600','to-blue-600','text-white');
    btn.classList.remove('glass-effect','text-gray-300');
}

// ── Modal: Add ────────────────────────────────────────────────
function openAddModal() {
    resetForm();
    setText('modalTitle', 'Add New Meal');
    setText('saveBtn', 'Save Meal');
    showEl('mealModal');
}

function openAddModalForDay(dayName) {
    resetForm();
    setVal('mealDay', dayName);
    setText('modalTitle', 'Add Meal \u2013 ' + dayName);
    setText('saveBtn', 'Save Meal');
    showEl('mealModal');
}

// ── Modal: Edit ───────────────────────────────────────────────
function openEditModal(id) {
    const m = meals.find(function(x) { return x.id === id; });
    if (!m) return;
    setVal('editId',       m.id);
    setVal('mealName',     m.name);
    setVal('mealType',     m.type);
    setVal('mealDay',      m.day);
    setVal('mealTime',     m.time || '');
    setVal('mealCalories', m.calories || '');
    setVal('mealProtein',  m.protein  || '');
    setVal('mealCarbs',    m.carbs    || '');
    setVal('mealFat',      m.fat      || '');
    setVal('mealNotes',    m.notes    || '');
    const rem = document.getElementById('mealReminder');
    if (rem) rem.checked = !!m.reminder;
    setText('modalTitle', 'Edit Meal');
    setText('saveBtn',    'Update Meal');
    showEl('mealModal');
}

function closeModal() {
    hideEl('mealModal');
    resetForm();
}

function resetForm() {
    const form = document.getElementById('mealForm');
    if (form) form.reset();
    setVal('editId', '');
}

// ── Save Meal ─────────────────────────────────────────────────
function saveMeal(e) {
    e.preventDefault();
    const id = getVal('editId');
    const meal = {
        id       : id || generateId(),
        name     : getVal('mealName').trim(),
        type     : getVal('mealType'),
        day      : getVal('mealDay'),
        time     : getVal('mealTime'),
        calories : parseInt(getVal('mealCalories'))  || 0,
        protein  : parseInt(getVal('mealProtein'))   || 0,
        carbs    : parseInt(getVal('mealCarbs'))     || 0,
        fat      : parseInt(getVal('mealFat'))       || 0,
        notes    : getVal('mealNotes').trim(),
        reminder : (document.getElementById('mealReminder') || {}).checked || false,
        createdAt: id ? ((meals.find(function(m) { return m.id === id; }) || {}).createdAt || new Date().toISOString())
                      : new Date().toISOString(),
    };

    if (id) {
        const idx = meals.findIndex(function(m) { return m.id === id; });
        if (idx > -1) meals[idx] = meal;
    } else {
        meals.push(meal);
    }

    persist();
    closeModal();
    renderAll();
    scheduleReminders();
    showToast(id ? '\u270F\uFE0F Meal updated!' : '\u2705 Meal added!', 'green');
}

// ── Delete ────────────────────────────────────────────────────
function openDeleteModal(id) {
    deleteTarget = id;
    showEl('deleteModal');
}
function closeDeleteModal() {
    deleteTarget = null;
    hideEl('deleteModal');
}
function confirmDelete() {
    if (!deleteTarget) return;
    meals = meals.filter(function(m) { return m.id !== deleteTarget; });
    persist();
    closeDeleteModal();
    renderAll();
    scheduleReminders();
    showToast('\uD83D\uDDD1\uFE0F Meal deleted.', 'red');
}

// ── Goal ──────────────────────────────────────────────────────
function updateGoal(val) {
    dailyGoal = Math.max(500, parseInt(val) || 2000);
    localStorage.setItem(GOAL_KEY, dailyGoal);
    renderSummary();
}

// ── Reminders ─────────────────────────────────────────────────
function requestReminderPermission() {
    if (!('Notification' in window)) {
        showToast('\u26A0\uFE0F Notifications not supported in this browser.', 'yellow');
        return;
    }
    if (Notification.permission === 'granted') {
        reminderEnabled = true;
        updateReminderUI();
        scheduleReminders();
        showToast('\uD83D\uDD14 Reminders enabled!', 'green');
    } else {
        Notification.requestPermission().then(function(perm) {
            reminderEnabled = perm === 'granted';
            updateReminderUI();
            scheduleReminders();
            showToast(reminderEnabled ? '\uD83D\uDD14 Reminders enabled!' : '\uD83D\uDD15 Permission denied.', reminderEnabled ? 'green' : 'red');
        });
    }
}

function updateReminderUI() {
    const enabled = reminderEnabled && (typeof Notification !== 'undefined') && Notification.permission === 'granted';
    const statusEl  = document.getElementById('reminderStatus');
    const dotEl     = document.getElementById('reminderDot');
    const btnTextEl = document.getElementById('reminderBtnText');

    if (enabled) {
        if (statusEl)  { statusEl.textContent = 'Reminders On';  statusEl.className = 'text-xs px-3 py-1 rounded-full bg-green-500/20 text-green-400 font-medium'; }
        if (dotEl)     dotEl.classList.remove('hidden');
        if (btnTextEl) btnTextEl.textContent = 'Reminders On';
    } else {
        if (statusEl)  { statusEl.textContent = 'Reminders Off'; statusEl.className = 'text-xs px-3 py-1 rounded-full bg-red-500/20 text-red-400 font-medium'; }
        if (dotEl)     dotEl.classList.add('hidden');
        if (btnTextEl) btnTextEl.textContent = 'Enable Reminders';
    }
}

function scheduleReminders() {
    reminderTimers.forEach(function(t) { clearTimeout(t); });
    reminderTimers = [];

    if (!reminderEnabled || (typeof Notification === 'undefined') || Notification.permission !== 'granted') {
        renderReminderList();
        return;
    }

    const now       = new Date();
    const todayName = DAYS[(now.getDay() + 6) % 7];
    const todayM    = meals.filter(function(m) { return m.reminder && m.day === todayName && m.time; });

    todayM.forEach(function(m) {
        const parts  = m.time.split(':');
        const h      = parseInt(parts[0]);
        const min    = parseInt(parts[1]);
        const target = new Date(now);
        target.setHours(h, min, 0, 0);
        const ms = target - now;
        if (ms > 0) {
            const timer = setTimeout(function() {
                new Notification('\uD83C\uDF7D\uFE0F Time for ' + m.name + '!', {
                    body: capitalize(m.type) + ' \u2013 ' + (m.calories ? m.calories + ' kcal' : 'no calorie info'),
                    icon: '/static/images/logo.png',
                    tag : m.id,
                });
            }, ms);
            reminderTimers.push(timer);
        }
    });
    renderReminderList();
}

function renderReminderList() {
    const container = document.getElementById('reminderList');
    if (!container) return;

    const todayName = DAYS[(new Date().getDay() + 6) % 7];
    const now       = new Date();
    const enabled   = reminderEnabled && (typeof Notification !== 'undefined') && Notification.permission === 'granted';

    if (!enabled) {
        container.innerHTML = '<p class="text-center py-4 text-gray-500">Enable reminders to see scheduled meal alerts.</p>';
        return;
    }

    const todayR = meals
        .filter(function(m) { return m.reminder && m.day === todayName && m.time; })
        .sort(function(a, b) { return a.time.localeCompare(b.time); });

    if (todayR.length === 0) {
        container.innerHTML = '<p class="text-center py-4 text-gray-500">No reminders set for today. Enable reminders on meals to see them here.</p>';
        return;
    }

    container.innerHTML = todayR.map(function(m) {
        const parts = m.time.split(':');
        const h   = parseInt(parts[0]);
        const min = parseInt(parts[1]);
        const t   = new Date();
        t.setHours(h, min, 0, 0);
        const past       = t < now;
        const bgClass    = past ? 'bg-gray-600/40' : 'bg-yellow-500/20';
        const bellClass  = past ? 'text-gray-500'  : 'text-yellow-400';
        const nameClass  = past ? 'text-gray-500 line-through' : 'text-white';
        const timeClass  = past ? 'text-gray-600'  : 'text-yellow-400';
        const macro      = m.calories ? m.calories + ' kcal' : 'no calorie info';

        return '<div class="flex items-center justify-between bg-white/5 rounded-xl px-4 py-3">' +
            '<div class="flex items-center gap-3">' +
                '<div class="w-8 h-8 rounded-full ' + bgClass + ' flex items-center justify-center">' +
                    '<i class="fas fa-bell ' + bellClass + ' text-xs"></i>' +
                '</div>' +
                '<div>' +
                    '<p class="text-sm font-medium ' + nameClass + '">' + escHtml(m.name) + '</p>' +
                    '<p class="text-xs text-gray-400 capitalize">' + m.type + ' \u00B7 ' + macro + '</p>' +
                '</div>' +
            '</div>' +
            '<span class="text-xs font-semibold ' + timeClass + '">' + formatTime(m.time) + '</span>' +
        '</div>';
    }).join('');
}

// ── CSV Export ────────────────────────────────────────────────
function exportCSV() {
    if (meals.length === 0) { showToast('\u26A0\uFE0F No meals to export.', 'yellow'); return; }

    const headers = ['ID','Name','Type','Day','Time','Calories (kcal)','Protein (g)','Carbs (g)','Fat (g)','Notes','Reminder','Created At'];
    const rows    = meals.map(function(m) {
        return [
            m.id, csvEsc(m.name), m.type, m.day, m.time || '',
            m.calories || 0, m.protein || 0, m.carbs || 0, m.fat || 0,
            csvEsc(m.notes || ''), m.reminder ? 'Yes' : 'No', m.createdAt || ''
        ];
    });

    const csv  = [headers].concat(rows).map(function(r) { return r.join(','); }).join('\n');
    const blob = new Blob([csv], {type:'text/csv;charset=utf-8;'});
    const url  = URL.createObjectURL(blob);
    const a    = document.createElement('a');
    a.href     = url;
    a.download = 'dietplan_' + new Date().toISOString().slice(0,10) + '.csv';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    showToast('\uD83D\uDCC1 CSV exported successfully!', 'green');
}

function csvEsc(val) {
    if (typeof val !== 'string') return val;
    return (val.indexOf(',') > -1 || val.indexOf('"') > -1 || val.indexOf('\n') > -1)
        ? '"' + val.replace(/"/g, '""') + '"'
        : val;
}

// ── CSV Import ────────────────────────────────────────────────
function importCSV(event) {
    const file = event.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = function(e) {
        const lines    = e.target.result.split('\n').filter(function(l) { return l.trim(); });
        const imported = [];
        for (let i = 1; i < lines.length; i++) {
            const cols = parseCsvLine(lines[i]);
            if (cols.length < 4) continue;
            imported.push({
                id       : cols[0] || generateId(),
                name     : cols[1] || ('Meal ' + i),
                type     : cols[2] || 'snack',
                day      : cols[3] || 'Monday',
                time     : cols[4] || '',
                calories : parseInt(cols[5])  || 0,
                protein  : parseInt(cols[6])  || 0,
                carbs    : parseInt(cols[7])  || 0,
                fat      : parseInt(cols[8])  || 0,
                notes    : cols[9]  || '',
                reminder : (cols[10] || '').toLowerCase() === 'yes',
                createdAt: cols[11] || new Date().toISOString(),
            });
        }
        meals = meals.concat(imported);
        persist();
        renderAll();
        showToast('\u2705 Imported ' + imported.length + ' meal(s).', 'green');
    };
    reader.readAsText(file);
    event.target.value = '';
}

function parseCsvLine(line) {
    const result = [];
    let cur = '', inQ = false;
    for (let i = 0; i < line.length; i++) {
        const c = line[i];
        if (c === '"')  { inQ = !inQ; continue; }
        if (c === ',' && !inQ) { result.push(cur.trim()); cur = ''; continue; }
        cur += c;
    }
    result.push(cur.trim());
    return result;
}

// ── Utilities ─────────────────────────────────────────────────
function capitalize(s)  { return s ? s.charAt(0).toUpperCase() + s.slice(1) : ''; }

function formatTime(t) {
    if (!t) return '';
    const parts = t.split(':');
    const h     = parseInt(parts[0]);
    const m     = parseInt(parts[1]);
    const ampm  = h >= 12 ? 'PM' : 'AM';
    return (h % 12 || 12) + ':' + String(m).padStart(2, '0') + ' ' + ampm;
}

function escHtml(str) {
    return String(str)
        .replace(/&/g,'&amp;')
        .replace(/</g,'&lt;')
        .replace(/>/g,'&gt;')
        .replace(/"/g,'&quot;');
}

function generateId() {
    return 'meal-' + Date.now() + '-' + Math.random().toString(36).slice(2, 9);
}

function setText(id, val) { const el = document.getElementById(id); if (el) el.textContent = val; }
function setVal(id, val)  { const el = document.getElementById(id); if (el) el.value = val; }
function getVal(id)       { const el = document.getElementById(id); return el ? el.value : ''; }
function showEl(id)       { const el = document.getElementById(id); if (el) el.classList.remove('hidden'); }
function hideEl(id)       { const el = document.getElementById(id); if (el) el.classList.add('hidden'); }

function showToast(msg, color) {
    color = color || 'green';
    const colors = {green:'bg-green-600', red:'bg-red-600', yellow:'bg-yellow-600', blue:'bg-blue-600'};
    const toast  = document.getElementById('toast');
    if (!toast) return;
    toast.className = 'fixed bottom-6 right-6 z-50 flex items-center gap-3 px-5 py-3 rounded-xl shadow-xl text-sm font-medium text-white animate-fade-in ' + (colors[color] || 'bg-green-600');
    toast.textContent = msg;
    toast.classList.remove('hidden');
    setTimeout(function() { toast.classList.add('hidden'); }, 3000);
}

// ── Keyboard: Esc closes modals ───────────────────────────────
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') { closeModal(); closeDeleteModal(); }
});
