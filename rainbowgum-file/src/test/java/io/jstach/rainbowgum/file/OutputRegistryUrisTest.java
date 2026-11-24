package io.jstach.rainbowgum.file;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.jstach.rainbowgum.LogConfig;
import io.jstach.rainbowgum.LogProperties;
import io.jstach.rainbowgum.RainbowGum;

/*
 * Finding the log files, the outputs themselves not being handed out.
 */
class OutputRegistryUrisTest {

	@Test
	void urisOfFileAndRollingOutputs(@TempDir Path dir) {
		Path file = dir.resolve("app.log");
		Path rolled = dir.resolve("rolled.log");
		var properties = LogProperties.builder().fromProperties("""
				logging.appenders=file,rolled
				logging.file.name=%s
				logging.appender.rolled.output.uri=rolling://%s?maxFileSize=1MB
				""".formatted(PropertiesPaths.escape(file), rolled.toUri().getRawPath())).build();
		var config = LogConfig.builder().properties(properties).serviceLoader().build();
		try (var gum = RainbowGum.builder(config).build().start()) {
			var uris = gum.config().outputRegistry().uris();
			assertEquals(List.of("file", "rolled"), List.copyOf(uris.keySet()));
			assertEquals(file, Path.of(uris.get("file")));
			// the active file, a plain file URI without the query
			assertEquals(URI.create("file://" + rolled.toUri().getRawPath()), uris.get("rolled"));
		}
	}

}
