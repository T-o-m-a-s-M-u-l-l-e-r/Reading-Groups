package com.muller_tomas.reading_groups.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.muller_tomas.reading_groups.exception.FileStorageException;
import com.muller_tomas.reading_groups.exception.InvalidGroupIconException;

@Service
public class GroupIconService {
	private final FileService fileService;
	private final long maxBytes;
	private final int maxDimension;
	private final int iconSize;

	public GroupIconService(FileService fileService,
			@Value("${app.group-icon.max-bytes}") long maxBytes,
			@Value("${app.group-icon.max-dimension}") int maxDimension,
			@Value("${app.group-icon.size}") int iconSize) {
		this.fileService = fileService;
		this.maxBytes = maxBytes;
		this.maxDimension = maxDimension;
		this.iconSize = iconSize;
	}

	public String save(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return null;
		}
		if (file.getSize() > maxBytes) {
			throw new InvalidGroupIconException("Group icon exceeds the upload size limit");
		}

		BufferedImage image = readImage(file);
		BufferedImage icon = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = icon.createGraphics();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			int side = Math.min(image.getWidth(), image.getHeight());
			int left = (image.getWidth() - side) / 2;
			int top = (image.getHeight() - side) / 2;
			graphics.drawImage(image, 0, 0, iconSize, iconSize, left, top, left + side, top + side, null);
		} finally {
			graphics.dispose();
		}

		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			ImageIO.write(icon, "png", output);
			return fileService.save(output.toByteArray(), "png");
		} catch (IOException exception) {
			throw new FileStorageException("Group icon could not be encoded", exception);
		}
	}

	private BufferedImage readImage(MultipartFile file) {
		try (InputStream input = file.getInputStream();
				ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {
			Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
			if (!readers.hasNext()) {
				throw new InvalidGroupIconException("Group icon must be a PNG or JPEG image");
			}
			ImageReader reader = readers.next();
			try {
				String format = reader.getFormatName();
				if (!format.equalsIgnoreCase("png") && !format.equalsIgnoreCase("jpeg")) {
					throw new InvalidGroupIconException("Group icon must be a PNG or JPEG image");
				}
				reader.setInput(imageInput);
				if (reader.getWidth(0) > maxDimension || reader.getHeight(0) > maxDimension) {
					throw new InvalidGroupIconException("Group icon dimensions exceed the limit");
				}
				return reader.read(0);
			} finally {
				reader.dispose();
			}
		} catch (IOException exception) {
			throw new InvalidGroupIconException("Group icon is not a valid image");
		}
	}

	public FileSystemResource load(String resourceId) {
		return fileService.load(resourceId, "png");
	}

	public void delete(String resourceId) {
		if (resourceId != null) {
			fileService.delete(resourceId, "png");
		}
	}
}
