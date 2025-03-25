package net.roxymc.slime.importer;

import java.io.File;
import java.io.IOException;

public interface SlimeImporter {
    ImportResult importWorld(File source) throws IOException;
}
