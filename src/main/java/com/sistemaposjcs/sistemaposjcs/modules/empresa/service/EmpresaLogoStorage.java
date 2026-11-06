package com.sistemaposjcs.sistemaposjcs.modules.empresa.service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface EmpresaLogoStorage {

    LogoAsset upload(MultipartFile file) throws IOException;

    void delete(String publicId) throws IOException;

    record LogoAsset(String url, String publicId) {
    }
}
