package de.redsix.pdfcompare;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;

public class Exclusions {

    final static ConfigParseOptions configParseOptions = ConfigParseOptions.defaults().setSyntax(ConfigSyntax.CONF).setAllowMissing(true);
    private final Map<Integer, PageExclusions> exclusionsPerPage = new HashMap<>();

    public void add(final Exclusion exclusion) {
        exclusionsPerPage.computeIfAbsent(exclusion.page, k -> new PageExclusions()).add(exclusion);
    }

    public boolean contains(final int page, final int x, final int y) {
        final PageExclusions exclusionsThisPage = exclusionsPerPage.get(page);
        if (exclusionsThisPage != null) {
            return exclusionsThisPage.contains(x, y);
        }
        return false;
    }

    public void readExclusions(final String filename) {
        if (filename != null) {
            readExclusions(new File(filename));
        }
    }

    public void readExclusions(final Path path) {
        if (path != null && Files.exists(path)) {
            readExclusions(path.toFile());
        }
    }

    public void readExclusions(final File file) {
        if (file != null && file.exists()) {
            final Config config = ConfigFactory.parseFile(file, configParseOptions);
            readFromConfig(config);
        }
    }

    public void readExclusions(InputStream inputStream) {
        if (inputStream != null) {
            readExclusions(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        }
    }

    public void readExclusions(Reader reader) {
        if (reader != null) {
            final Config config = ConfigFactory.parseReader(reader, configParseOptions);
            readFromConfig(config);
        }
    }

    private void readFromConfig(final Config load) {
        final List<? extends ConfigObject> exclusions = load.getObjectList("exclusions");
        exclusions.stream().map(co -> {
            final Config c = co.toConfig();
            return new Exclusion(c.getInt("page"), c.getInt("x1"), c.getInt("y1"), c.getInt("x2"), c.getInt("y2"));
        }).forEach(e -> add(e));
    }

    public PageExclusions forPage(final int page) {
        return exclusionsPerPage.getOrDefault(page, new PageExclusions());
    }
}
