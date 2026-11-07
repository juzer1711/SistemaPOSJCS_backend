package com.sistemaposjcs.sistemaposjcs.modules.empresa.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CloudinaryEmpresaLogoStorage implements EmpresaLogoStorage {

    private static final long DEFAULT_MAX_FILE_SIZE = 2 * 1024 * 1024;
    private static final int MAX_IMAGE_DIMENSION = 4096;
    private static final long MAX_IMAGE_PIXELS = 16_000_000;

    private final Cloudinary cloudinary;
    private final String namespace;
    private final long maxFileSize;

    public CloudinaryEmpresaLogoStorage(
            Cloudinary cloudinary,
            @Value("${app.empresa.logo.namespace:}") String namespace,
            @Value("${app.empresa.logo.max-size-bytes:2097152}") long maxFileSize
    ) {
        this.cloudinary = cloudinary;
        this.namespace = namespace;
        this.maxFileSize = maxFileSize > 0 ? maxFileSize : DEFAULT_MAX_FILE_SIZE;
    }

    @Override
    public LogoAsset upload(MultipartFile file) throws IOException {
        validarImagen(file);
        validarNamespace();

        String publicId = "pos_system/empresa_logos/" + namespace + "/" + UUID.randomUUID();
        Map<?, ?> result;
        try {
            result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "overwrite", false,
                            "resource_type", "image"
                    )
            );
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No fue posible guardar el logo en Cloudinary",
                    exception
            );
        }
        Object secureUrl = result.get("secure_url");
        if (!(secureUrl instanceof String url) || url.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "El proveedor de imágenes no devolvió una URL segura"
            );
        }

        return new LogoAsset(url, publicId);
    }

    @Override
    public void delete(String publicId) throws IOException {
        if (publicId != null && !publicId.isBlank()) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }

    private void validarImagen(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes seleccionar una imagen");
        }
        if (file.getSize() > maxFileSize) {
            throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE, "El logo no puede superar 2 MB");
        }

        try (InputStream input = file.getInputStream();
                ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {
            if (imageInput == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo no es una imagen válida");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se admiten imágenes PNG o JPEG");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput, true, true);
                String format = reader.getFormatName();
                if (!format.equalsIgnoreCase("png")
                        && !format.equalsIgnoreCase("jpg")
                        && !format.equalsIgnoreCase("jpeg")) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Solo se admiten imágenes PNG o JPEG"
                    );
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > MAX_IMAGE_DIMENSION
                        || height > MAX_IMAGE_DIMENSION || (long) width * height > MAX_IMAGE_PIXELS) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Las dimensiones del logo exceden el límite permitido"
                    );
                }
            } finally {
                reader.dispose();
            }
        }
    }

    private void validarNamespace() {
        if (namespace == null || !namespace.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Configura EMPRESA_LOGO_NAMESPACE con un identificador único para esta instalación"
            );
        }
    }
}
