package com.example.OOP_FitConnect.repository;

import com.example.OOP_FitConnect.model.DietPlan;
import com.example.OOP_FitConnect.model.Meal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class InstructorDietPlanRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<DietPlan> planMapper = (rs, rowNum) -> {
        DietPlan p = new DietPlan();
        p.setId(rs.getLong("id"));
        p.setPlanName(rs.getString("plan_name"));
        p.setInstructorId(rs.getInt("instructor_id"));
        int mid = rs.getInt("member_id");
        p.setMemberId(rs.wasNull() ? null : mid);
        p.setGoal(rs.getString("goal"));
        int cal = rs.getInt("daily_calories");
        p.setDailyCalories(rs.wasNull() ? null : cal);
        int dur = rs.getInt("duration_weeks");
        p.setDurationWeeks(rs.wasNull() ? null : dur);
        int prot = rs.getInt("protein_grams");
        p.setProteinGrams(rs.wasNull() ? null : prot);
        int carb = rs.getInt("carbs_grams");
        p.setCarbsGrams(rs.wasNull() ? null : carb);
        int fat = rs.getInt("fat_grams");
        p.setFatGrams(rs.wasNull() ? null : fat);
        p.setNotes(rs.getString("notes"));
        p.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) p.setCreatedAt(ts.toLocalDateTime());
        // Optional join columns — swallow errors if not present
        try { p.setInstructorName(rs.getString("instructor_name")); } catch (Exception ignored) {}
        try { p.setMemberName(rs.getString("member_name")); } catch (Exception ignored) {}
        return p;
    };

    private final RowMapper<Meal> mealMapper = (rs, rowNum) -> {
        Meal m = new Meal();
        m.setId(rs.getLong("id"));
        m.setDietPlanId(rs.getLong("diet_plan_id"));
        m.setMealType(rs.getString("meal_type"));
        m.setFoodItems(rs.getString("food_items"));
        int cal = rs.getInt("calories");
        m.setCalories(rs.wasNull() ? null : cal);
        return m;
    };

    // ── Diet Plan CRUD ─────────────────────────────────────────────

    public DietPlan save(DietPlan plan) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO diet_plans (plan_name, instructor_id, member_id, goal, daily_calories, " +
                    "duration_weeks, protein_grams, carbs_grams, fat_grams, notes, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, plan.getPlanName());
            ps.setInt(2, plan.getInstructorId());
            setNullableInt(ps, 3, plan.getMemberId());
            ps.setString(4, plan.getGoal());
            setNullableInt(ps, 5, plan.getDailyCalories());
            setNullableInt(ps, 6, plan.getDurationWeeks());
            setNullableInt(ps, 7, plan.getProteinGrams());
            setNullableInt(ps, 8, plan.getCarbsGrams());
            setNullableInt(ps, 9, plan.getFatGrams());
            ps.setString(10, plan.getNotes());
            ps.setString(11, plan.getStatus() != null ? plan.getStatus() : "ACTIVE");
            return ps;
        }, kh);
        plan.setId(kh.getKey().longValue());
        saveMeals(plan.getId(), plan.getMeals());
        return plan;
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM diet_plan_meals WHERE diet_plan_id = ?", id);
        jdbcTemplate.update("DELETE FROM diet_plans WHERE id = ?", id);
    }

    public List<DietPlan> findByInstructorId(int instructorId) {
        String sql = "SELECT dp.*, u.name AS member_name FROM diet_plans dp " +
                     "LEFT JOIN users u ON dp.member_id = u.id " +
                     "WHERE dp.instructor_id = ? ORDER BY dp.created_at DESC";
        List<DietPlan> plans = jdbcTemplate.query(sql, planMapper, instructorId);
        plans.forEach(p -> p.setMeals(findMealsByPlanId(p.getId())));
        return plans;
    }

    public List<DietPlan> findByMemberId(int memberId) {
        String sql = "SELECT dp.*, u.name AS instructor_name FROM diet_plans dp " +
                     "LEFT JOIN users u ON dp.instructor_id = u.id " +
                     "WHERE dp.member_id = ? ORDER BY dp.created_at DESC";
        List<DietPlan> plans = jdbcTemplate.query(sql, planMapper, memberId);
        plans.forEach(p -> p.setMeals(findMealsByPlanId(p.getId())));
        return plans;
    }

    public List<DietPlan> findAll() {
        String sql = "SELECT dp.*, " +
                     "ins.name AS instructor_name, mem.name AS member_name " +
                     "FROM diet_plans dp " +
                     "LEFT JOIN users ins ON dp.instructor_id = ins.id " +
                     "LEFT JOIN users mem ON dp.member_id = mem.id " +
                     "ORDER BY dp.created_at DESC";
        List<DietPlan> plans = jdbcTemplate.query(sql, planMapper);
        plans.forEach(p -> p.setMeals(findMealsByPlanId(p.getId())));
        return plans;
    }

    public int countByInstructorId(int instructorId) {
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM diet_plans WHERE instructor_id = ?", Integer.class, instructorId);
        return c != null ? c : 0;
    }

    // ── Meals ──────────────────────────────────────────────────────

    private void saveMeals(Long planId, List<Meal> meals) {
        if (meals == null || meals.isEmpty()) return;
        for (Meal m : meals) {
            jdbcTemplate.update(
                    "INSERT INTO diet_plan_meals (diet_plan_id, meal_type, food_items, calories) VALUES (?, ?, ?, ?)",
                    planId, m.getMealType(), m.getFoodItems(), m.getCalories());
        }
    }

    public List<Meal> findMealsByPlanId(Long planId) {
        return jdbcTemplate.query(
                "SELECT * FROM diet_plan_meals WHERE diet_plan_id = ? ORDER BY id",
                mealMapper, planId);
    }

    // ── Helpers ────────────────────────────────────────────────────

    private void setNullableInt(PreparedStatement ps, int idx, Integer value) throws java.sql.SQLException {
        if (value == null) ps.setNull(idx, java.sql.Types.INTEGER);
        else ps.setInt(idx, value);
    }
}
