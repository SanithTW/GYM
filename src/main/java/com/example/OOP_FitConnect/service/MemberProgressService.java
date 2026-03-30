package com.example.OOP_FitConnect.service;

import com.example.OOP_FitConnect.model.MemberProgress;
import com.example.OOP_FitConnect.repository.MemberProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberProgressService {

    @Autowired
    private MemberProgressRepository repository;

    public void logProgress(MemberProgress progress) {
        repository.saveProgress(progress);
    }

    public void deleteProgress(Long id) {
        repository.deleteProgress(id);
    }

    public List<MemberProgress> getProgressForMember(int memberId) {
        return repository.findProgressByMemberId(memberId);
    }
}
