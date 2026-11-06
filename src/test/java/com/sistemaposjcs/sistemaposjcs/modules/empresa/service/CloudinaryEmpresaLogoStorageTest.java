package com.sistemaposjcs.sistemaposjcs.modules.empresa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class CloudinaryEmpresaLogoStorageTest {

    @Test
    void rejectsContentThatIsNotAnImageRegardlessOfItsMimeType() {
        CloudinaryEmpresaLogoStorage storage = new CloudinaryEmpresaLogoStorage(
                mock(Cloudinary.class),
                "test-installation",
                2 * 1024 * 1024
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.png",
                "image/png",
                "not really an image".getBytes()
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> storage.upload(file)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void rejectsFilesAboveConfiguredLimitBeforeContactingCloudinary() {
        CloudinaryEmpresaLogoStorage storage = new CloudinaryEmpresaLogoStorage(
                mock(Cloudinary.class),
                "test-installation",
                3
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.png",
                "image/png",
                new byte[] {1, 2, 3, 4}
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> storage.upload(file)
        );

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, exception.getStatusCode());
    }
}
