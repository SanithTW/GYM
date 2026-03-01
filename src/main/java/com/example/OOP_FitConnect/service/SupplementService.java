package com.example.OOP_FitConnect.service;

import com.example.OOP_FitConnect.model.Supplement;
import com.example.OOP_FitConnect.repository.SupplementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class SupplementService {

    private final SupplementRepository supplementRepository;
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/supplements/";

    @Autowired
    public SupplementService(SupplementRepository supplementRepository) {
        this.supplementRepository = supplementRepository;
    }

    public List<Supplement> getAllSupplements() {
        return supplementRepository.findAll();
    }

    public Supplement getSupplementById(Long id) {
        return supplementRepository.findById(id);
    }

    public Supplement addSupplement(Supplement supplement, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(imageFile.getInputStream(), filePath);
            supplement.setImagePath("/uploads/supplements/" + fileName);
        }
        return supplementRepository.save(supplement);
    }

    public void deleteSupplement(Long id) {
        supplementRepository.deleteById(id);
    }
}
