package com.abctelecom.nms.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvUtilTest {

    @Test
    void escapeQuotesOnlyWhenNeeded() {
        assertEquals("plain", CsvUtil.escape("plain"));
        assertEquals("\"a,b\"", CsvUtil.escape("a,b"));
        assertEquals("\"he said \"\"hi\"\"\"", CsvUtil.escape("he said \"hi\""));
        assertEquals("", CsvUtil.escape(null));
    }

    @Test
    void rowJoinsWithCommas() {
        // escape() quotes a field that starts with a quote: internal " doubles to ""
        assertEquals("a,b,\"\"\"c\"", CsvUtil.row(List.of("a", "b", "\"c")));
        assertEquals("x,y", CsvUtil.row(List.of("x", "y")));
    }

    @Test
    void parseLineRoundTripsQuotedFields() {
        String line = CsvUtil.row(List.of("id-1", "name, with comma", "quote\"inside"));
        String[] parts = CsvUtil.parseLine(line);
        assertArrayEquals(new String[]{"id-1", "name, with comma", "quote\"inside"}, parts);
    }

    @Test
    void writeCreatesHeaderAndRows(@TempDir Path tmp) throws IOException {
        Path f = tmp.resolve("out/data.csv");
        CsvUtil.write(f, List.of("a", "b"), List.of(List.of("1", "x,y"), List.of("2", "q")));
        List<String> lines = Files.readAllLines(f);
        assertEquals(3, lines.size());
        assertEquals("a,b", lines.get(0));
        assertEquals("1,\"x,y\"", lines.get(1));
    }
}
