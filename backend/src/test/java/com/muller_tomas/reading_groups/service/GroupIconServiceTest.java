package com.muller_tomas.reading_groups.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.muller_tomas.reading_groups.exception.InvalidGroupIconException;
import com.muller_tomas.reading_groups.exception.StoredFileNotFoundException;

public class GroupIconServiceTest {
	@TempDir
	public Path storageRoot;

	@Test
	public void omittedIconUsesDefault() {
		GroupIconService service = new GroupIconService(new FileService(storageRoot.toString()), 2097152, 4096, 256);
		assertNull(service.save(null));
	}

	@Test
	public void normalizesUploadedImageAndDeletesStoredIcon() throws Exception {
		GroupIconService service = new GroupIconService(new FileService(storageRoot.toString()), 2097152, 4096, 256);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ImageIO.write(new BufferedImage(600, 300, BufferedImage.TYPE_INT_RGB), "jpeg", bytes);
		MockMultipartFile upload = new MockMultipartFile("icon", "photo.jpg", "image/jpeg", bytes.toByteArray());
		String resourceId = service.save(upload);
		BufferedImage stored = ImageIO.read(service.load(resourceId).getFile());
		assertEquals(256, stored.getWidth());
		assertEquals(256, stored.getHeight());
		service.delete(resourceId);
		assertThrows(StoredFileNotFoundException.class, () -> service.load(resourceId));
	}

	@Test
	public void rejectsFakeImageEvenWhenItsContentTypeSaysPng() {
		GroupIconService service = new GroupIconService(new FileService(storageRoot.toString()), 2097152, 4096, 256);
		MockMultipartFile upload = new MockMultipartFile("icon", "fake.png", "image/png", "not an image".getBytes());
		assertThrows(InvalidGroupIconException.class, () -> service.save(upload));
	}

	@Test
	public void rejectsOversizedFilesAndImageDimensions() throws Exception {
		GroupIconService service = new GroupIconService(new FileService(storageRoot.toString()), 100, 10, 8);
		MockMultipartFile largeUpload = new MockMultipartFile("icon", new byte[101]);
		assertThrows(InvalidGroupIconException.class, () -> service.save(largeUpload));
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ImageIO.write(new BufferedImage(11, 1, BufferedImage.TYPE_INT_RGB), "png", bytes);
		MockMultipartFile wideUpload = new MockMultipartFile("icon", bytes.toByteArray());
		assertThrows(InvalidGroupIconException.class, () -> service.save(wideUpload));
	}
}
