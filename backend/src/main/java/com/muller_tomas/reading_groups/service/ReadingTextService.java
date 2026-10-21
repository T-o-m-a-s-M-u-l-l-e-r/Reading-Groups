package com.muller_tomas.reading_groups.service;

import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.muller_tomas.reading_groups.exception.InvalidReadingTextException;

@Service
public class ReadingTextService {
	private final FileService fileService;

	public ReadingTextService(FileService fileService) {
		this.fileService = fileService;
	}

	public String save(MultipartFile pdfFile) {
		if (pdfFile == null || pdfFile.isEmpty()) {
			throw new InvalidReadingTextException("Reading text is empty");
		}

		byte[] contents;
		try {
			contents = pdfFile.getBytes();
			try (PDDocument document = Loader.loadPDF(contents)) {
				if (document.isEncrypted()) {
					throw new InvalidReadingTextException("The file is encrypted");
				}
			}
		} catch (IOException exception) {
			throw new InvalidReadingTextException("File is not a valid pdf file");
		}

		return fileService.save(contents, "pdf");
	}

	public FileSystemResource load(String resourceId) {
		return fileService.load(resourceId, "pdf");
	}

	public void delete(String resourceId) {
		fileService.delete(resourceId, "pdf");
	}
}
