package com.sistemaposjcs.sistemaposjcs.modules.empresa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import com.cloudinary.Cloudinary;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class CloudinaryEmpresaLogoStorageTest {

    @Test
    void aceptaPngRealYUsaUrlSeguraConNamespacePropio() throws IOException {
        Cloudinary cloudinary = mock(Cloudinary.class, RETURNS_DEEP_STUBS);
        when(cloudinary.uploader().upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of("secure_url", "https://example.com/logo.png"));
        var storage = new CloudinaryEmpresaLogoStorage(cloudinary, "test-installation", 2097152);
        var asset = storage.upload(imagen("png", 8, 8));
        assertEquals("https://example.com/logo.png", asset.url());
        org.junit.jupiter.api.Assertions.assertTrue(asset.publicId().startsWith("pos_system/empresa_logos/test-installation/"));
    }

    @Test
    void rechazaGifAunqueElMimeDeclarePng() throws IOException {
        Cloudinary cloudinary = mock(Cloudinary.class);
        var storage = new CloudinaryEmpresaLogoStorage(cloudinary, "test-installation", 2097152);
        var exception = assertThrows(ResponseStatusException.class, () -> storage.upload(imagen("gif", 8, 8)));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(cloudinary);
    }

    @Test
    void rechazaDimensionesExcesivasAntesDeSubir() throws IOException {
        Cloudinary cloudinary = mock(Cloudinary.class);
        var storage = new CloudinaryEmpresaLogoStorage(cloudinary, "test-installation", 2097152);
        var exception = assertThrows(ResponseStatusException.class, () -> storage.upload(imagen("png", 4097, 1)));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(cloudinary);
    }

    @Test
    void rechazaArchivoVacio() {
        var storage = new CloudinaryEmpresaLogoStorage(mock(Cloudinary.class), "test-installation", 2097152);
        var exception = assertThrows(ResponseStatusException.class, () -> storage.upload(
                new MockMultipartFile("file", "logo.png", "image/png", new byte[0])));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void namespaceInvalidoNoContactaAlProveedor() throws IOException {
        Cloudinary cloudinary = mock(Cloudinary.class);
        var storage = new CloudinaryEmpresaLogoStorage(cloudinary, "", 2097152);
        var exception = assertThrows(ResponseStatusException.class, () -> storage.upload(imagen("png", 8, 8)));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
        verifyNoInteractions(cloudinary);
    }

    @Test
    void falloDelProveedorSeTraduceA502() throws IOException {
        Cloudinary cloudinary = mock(Cloudinary.class, RETURNS_DEEP_STUBS);
        when(cloudinary.uploader().upload(any(byte[].class), anyMap())).thenThrow(new IOException("fallo simulado"));
        var storage = new CloudinaryEmpresaLogoStorage(cloudinary, "test-installation", 2097152);
        var exception = assertThrows(ResponseStatusException.class, () -> storage.upload(imagen("png", 8, 8)));
        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
    }

    private MockMultipartFile imagen(String format, int width, int height) throws IOException {
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), format, output);
        return new MockMultipartFile("file", "logo." + format, "image/png", output.toByteArray());
    }

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

        assertEquals(HttpStatus.CONTENT_TOO_LARGE, exception.getStatusCode());
    }
}
