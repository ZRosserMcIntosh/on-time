package com.ontime;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Auto-updater for On Time.
 *
 * On startup, checks GitHub Releases for a newer version.
 * If found, downloads the new jar and replaces the running one.
 *
 * Update flow:
 *   1. Game launches → applyPendingUpdate() runs first (handles deferred updates)
 *   2. checkAndUpdate() hits GitHub API for latest release
 *   3. Compares semver (CURRENT_VERSION vs tag_name)
 *   4. Downloads the right asset (.jar for mac/linux, .exe for windows)
 *   5. Replaces the running jar in-place (or defers to next restart if locked)
 *   6. Returns true if a restart is needed
 *
 * Release workflow (for developer):
 *   1. Bump VERSION in Main.java and CURRENT_VERSION here
 *   2. `mvn package`
 *   3. Create GitHub Release with tag "v0.X.0"
 *   4. Attach on-time-X.X.X.jar as release asset
 *   5. Users get it automatically next launch
 */
public class AutoUpdater {

    public static final String CURRENT_VERSION = Main.VERSION;

    private static final String GITHUB_API = "https://api.github.com/repos/ZRosserMcIntosh/on-time/releases/latest";
    private static final String USER_AGENT = "OnTime-Updater/" + CURRENT_VERSION;
    private static final int TIMEOUT_MS = 8000;

    /**
     * Check for updates and apply if available.
     * Returns true if the game should restart with the new version.
     */
    public static boolean checkAndUpdate() {
        try {
            System.out.println("[Updater] Checking for updates...");
            System.out.println("[Updater] Current version: " + CURRENT_VERSION);

            String releaseJson = httpGet(GITHUB_API);
            if (releaseJson == null) {
                System.out.println("[Updater] Could not reach GitHub. Skipping update check.");
                return false;
            }

            String latestVersion = parseLatestVersion(releaseJson);
            if (latestVersion == null) {
                System.out.println("[Updater] Could not parse release version. Skipping.");
                return false;
            }

            System.out.println("[Updater] Latest version: " + latestVersion);

            if (!isNewerVersion(latestVersion, CURRENT_VERSION)) {
                System.out.println("[Updater] Already up to date!");
                return false;
            }

            System.out.println("[Updater] New version available: " + latestVersion);

            String downloadUrl = findDownloadUrl(releaseJson);
            if (downloadUrl == null) {
                System.out.println("[Updater] Could not find download URL. Skipping.");
                return false;
            }

            System.out.println("[Updater] Downloading update from: " + downloadUrl);

            Path currentJar = getCurrentJarPath();
            if (currentJar == null) {
                System.out.println("[Updater] Not running from a JAR. Skipping auto-update (dev mode).");
                return false;
            }

            Path tempFile = currentJar.getParent().resolve(".ontime-update.tmp");
            if (!downloadFile(downloadUrl, tempFile)) {
                System.out.println("[Updater] Download failed. Skipping.");
                Files.deleteIfExists(tempFile);
                return false;
            }

            long size = Files.size(tempFile);
            if (size < 1_000_000) {
                System.out.println("[Updater] Downloaded file too small (" + size + " bytes). Skipping.");
                Files.deleteIfExists(tempFile);
                return false;
            }

            System.out.println("[Updater] Downloaded " + (size / 1024 / 1024) + " MB");

            // Backup current version
            Path backup = currentJar.getParent().resolve(currentJar.getFileName() + ".backup");
            try {
                Files.copy(currentJar, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("[Updater] Could not backup current version: " + e.getMessage());
            }

            // Replace current file with update
            try {
                Files.move(tempFile, currentJar, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[Updater] ✓ Update applied! Restarting...");
                return true;
            } catch (IOException e) {
                // On Windows, the running jar/exe may be locked
                System.out.println("[Updater] Could not replace running file. Deferring to next restart...");
                try {
                    Path updateReady = currentJar.getParent().resolve(".ontime-update-ready");
                    Files.move(tempFile, updateReady, StandardCopyOption.REPLACE_EXISTING);
                    writeUpdateScript(updateReady, currentJar);
                    System.out.println("[Updater] Update downloaded! Will be applied on next restart.");
                    return false;
                } catch (IOException e2) {
                    System.out.println("[Updater] Auto-update failed: " + e2.getMessage());
                    Files.deleteIfExists(tempFile);
                    return false;
                }
            }

        } catch (Exception e) {
            System.out.println("[Updater] Update check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Apply a pending update that was deferred from a previous run.
     */
    public static void applyPendingUpdate() {
        try {
            Path currentJar = getCurrentJarPath();
            if (currentJar == null) return;

            Path updateReady = currentJar.getParent().resolve(".ontime-update-ready");
            if (Files.exists(updateReady)) {
                System.out.println("[Updater] Applying pending update...");
                Files.move(updateReady, currentJar, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[Updater] ✓ Update applied!");
                Path backup = currentJar.getParent().resolve(currentJar.getFileName() + ".backup");
                Files.deleteIfExists(backup);
            }
        } catch (Exception e) {
            System.out.println("[Updater] Could not apply pending update: " + e.getMessage());
        }
    }

    // ── Version parsing ──────────────────────────────────────────

    private static String parseLatestVersion(String json) {
        String version = parseJsonField(json, "tag_name");
        if (version == null) return null;

        version = version.replaceAll("^[vV]", "").trim();

        // Check body for explicit "Version: X.Y.Z"
        String body = parseJsonField(json, "body");
        if (body != null) {
            Matcher m = Pattern.compile("Version:\\s*(\\d+\\.\\d+\\.\\d+)").matcher(body);
            if (m.find()) return m.group(1);
        }

        // If tag is "latest", try release name
        if (version.equalsIgnoreCase("latest")) {
            String name = parseJsonField(json, "name");
            if (name != null) {
                Matcher m = Pattern.compile("(\\d+\\.\\d+\\.\\d+)").matcher(name);
                if (m.find()) return m.group(1);
            }
        }

        return version;
    }

    private static String findDownloadUrl(String json) {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");

        Pattern urlPattern = Pattern.compile("\"browser_download_url\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = urlPattern.matcher(json);

        String jarUrl = null;
        String exeUrl = null;

        while (matcher.find()) {
            String url = matcher.group(1);
            if (url.endsWith(".exe")) exeUrl = url;
            else if (url.endsWith(".jar")) jarUrl = url;
        }

        if (isWindows && exeUrl != null) return exeUrl;
        if (jarUrl != null) return jarUrl;
        return exeUrl;
    }

    // ── Networking ───────────────────────────────────────────────

    private static boolean downloadFile(String urlStr, Path dest) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);

            if (conn.getResponseCode() != 200) {
                System.out.println("[Updater] HTTP " + conn.getResponseCode());
                return false;
            }

            long total = conn.getContentLengthLong();
            try (InputStream in = conn.getInputStream();
                 OutputStream out = Files.newOutputStream(dest)) {
                byte[] buf = new byte[8192];
                long downloaded = 0;
                int n, lastPercent = -1;
                while ((n = in.read(buf)) != -1) {
                    out.write(buf, 0, n);
                    downloaded += n;
                    if (total > 0) {
                        int percent = (int) (downloaded * 100 / total);
                        if (percent != lastPercent && percent % 10 == 0) {
                            System.out.println("[Updater] Downloading... " + percent + "%");
                            lastPercent = percent;
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("[Updater] Download error: " + e.getMessage());
            return false;
        }
    }

    private static String httpGet(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);

            if (conn.getResponseCode() != 200) return null;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                return sb.toString();
            }
        } catch (Exception e) {
            return null;
        }
    }

    // ── Helpers ──────────────────────────────────────────────────

    private static String parseJsonField(String json, String field) {
        Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        if (m.find()) return m.group(1).replace("\\n", "\n").replace("\\r", "");
        return null;
    }

    static boolean isNewerVersion(String latest, String current) {
        try {
            int[] l = parseVersion(latest), c = parseVersion(current);
            for (int i = 0; i < 3; i++) {
                if (l[i] > c[i]) return true;
                if (l[i] < c[i]) return false;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static int[] parseVersion(String version) {
        String[] parts = version.split("\\.");
        int[] result = new int[3];
        for (int i = 0; i < Math.min(parts.length, 3); i++) {
            result[i] = Integer.parseInt(parts[i].replaceAll("[^0-9]", ""));
        }
        return result;
    }

    private static Path getCurrentJarPath() {
        try {
            String path = AutoUpdater.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath();
            if (System.getProperty("os.name").toLowerCase().contains("win") && path.startsWith("/")) {
                path = path.substring(1);
            }
            Path jarPath = Paths.get(path);
            if (Files.isRegularFile(jarPath) && (path.endsWith(".jar") || path.endsWith(".exe"))) {
                return jarPath;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static void writeUpdateScript(Path updateFile, Path targetFile) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        Path scriptPath;

        if (os.contains("win")) {
            scriptPath = targetFile.getParent().resolve(".ontime-apply-update.bat");
            String script = "@echo off\r\ntimeout /t 2 /nobreak >nul\r\n"
                    + "move /Y \"" + updateFile + "\" \"" + targetFile + "\"\r\n"
                    + "start \"\" \"" + targetFile + "\"\r\ndel \"%~f0\"\r\n";
            Files.writeString(scriptPath, script);
        } else {
            scriptPath = targetFile.getParent().resolve(".ontime-apply-update.sh");
            String script = "#!/bin/bash\nsleep 2\n"
                    + "mv \"" + updateFile + "\" \"" + targetFile + "\"\n"
                    + "chmod +x \"" + targetFile + "\"\n"
                    + "\"" + targetFile + "\" &\nrm -- \"$0\"\n";
            Files.writeString(scriptPath, script);
            scriptPath.toFile().setExecutable(true);
        }
    }
}
