package com.muller_tomas.reading_groups.service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.muller_tomas.reading_groups.exception.FileStorageException;
import com.muller_tomas.reading_groups.exception.InvalidResourceIdException;
import com.muller_tomas.reading_groups.exception.StoredFileNotFoundException;

@Component
public class FileService {
	private final Path storageRootPath;

	public FileService(@Value("${app.storage.root}") String rootFolderConfiguration) {
		storageRootPath = Paths.get(rootFolderConfiguration);

		if (Files.exists(storageRootPath)) {

			if (!Files.isDirectory(storageRootPath)) {
				throw new IllegalStateException("Storage path is not a directory: " + storageRootPath);
			}

			if (!Files.isWritable(storageRootPath)) {
				throw new IllegalStateException("Storage path is not writable: " + storageRootPath);
			}

		} else {

			try {
				Files.createDirectory(storageRootPath);
			} catch (IOException e) {
				throw new IllegalStateException("Could not create storage directory: " + storageRootPath, e);
			}

		}

	}

	public String save(byte[] contents, String extension) {

		validateExtension(extension);
		while (true) {
			String resourceId = UUID.randomUUID().toString();
			Path newPath = storageRootPath.resolve(resourceId + "." + extension);

			try (OutputStream outputStream = Files.newOutputStream(newPath, StandardOpenOption.CREATE_NEW)) {

				outputStream.write(contents);
				return resourceId;

			} catch (FileAlreadyExistsException e) {

			} catch (IOException e) {
				throw new FileStorageException("File could not be saved: " + resourceId, e);
			}
		}

	}

	public FileSystemResource load(String resourceId, String extension) {
		validateExtension(extension);
		validateResourceId(resourceId);
		Path path = storageRootPath.resolve(resourceId + "." + extension);

		if (!Files.exists(path)) {
			throw new StoredFileNotFoundException("File not found: " + resourceId);
		}

		if (!Files.isReadable(path)) {
			throw new FileStorageException("Loaded file is not readable: " + resourceId);
		}

		try {
			if (Files.size(path) == 0) {
				throw new FileStorageException("Loaded file is empty: " + resourceId);
			}
		} catch (IOException e) {
			throw new FileStorageException("Failed to load stored file: " + resourceId, e);
		}

		return new FileSystemResource(path);
	}

	public void delete(String resourceId, String extension) {
		validateExtension(extension);
		validateResourceId(resourceId);
		Path filePath = storageRootPath.resolve(resourceId + "." + extension);

		try {
			Files.deleteIfExists(filePath);
		} catch (IOException e) {
			throw new FileStorageException("Failed to delete stored file: " + resourceId, e);
		}
	}
	
	private void validateExtension(String extension) {
		if (extension == null || !extension.matches("[a-z0-9]+")) {
			throw new IllegalArgumentException("Invalid file extension");
		}
	}

	private void validateResourceId(String resourceId) {
		
		try {
			UUID.fromString(resourceId);
		} catch (IllegalArgumentException e) {
			throw new InvalidResourceIdException("Resource ID is invalid", e);
		}
		
	}

}
