package net.roxymc.slime.importer;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public interface SlimeImporter {
    Set<String> preservedWorldTags();

    Set<String> preservedChunkTags();

    ImportResult importWorld(File source) throws IOException;
}
