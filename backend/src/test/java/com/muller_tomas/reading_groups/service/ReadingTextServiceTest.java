package com.muller_tomas.reading_groups.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.muller_tomas.reading_groups.exception.InvalidReadingTextException;
import com.muller_tomas.reading_groups.exception.StoredFileNotFoundException;

public class ReadingTextServiceTest {
	@TempDir
	public Path storageRoot;

	@Test
	public void validPdfIsStoredUnchangedAndCanBeDeleted() throws Exception {
		ReadingTextService service = new ReadingTextService(new FileService(storageRoot.toString()));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (PDDocument document = new PDDocument()) {
			document.addPage(new PDPage());
			document.save(output);
		}
		byte[] contents = output.toByteArray();
		String resourceId = service.save(new MockMultipartFile("readingText", contents));

		assertArrayEquals(contents, Files.readAllBytes(service.load(resourceId).getFile().toPath()));
		service.delete(resourceId);
		assertThrows(StoredFileNotFoundException.class, () -> service.load(resourceId));
	}

	@Test
	public void emptyAndInvalidReadingTextsAreRejected() {
		ReadingTextService service = new ReadingTextService(new FileService(storageRoot.toString()));
		assertThrows(InvalidReadingTextException.class, () -> service.save(null));
		MockMultipartFile invalidPdf = new MockMultipartFile("readingText", new byte[] { 1, 2, 3 });
		assertThrows(InvalidReadingTextException.class, () -> service.save(invalidPdf));
	}
}
