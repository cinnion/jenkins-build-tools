package org.ka8zrt

/**
 * A base utility class providing what will likely be some common helper
 * methods.
 */
abstract class UtilityBaseClass implements Serializable {

    /**
     * Create a temporary location for holding a script.
     *
     * @param path A string representing a file name.
     * @return A path to a temporary file.
     */
    static String createTempLocation(String path) {
        String tmpDir = pwd tmp: true
        return tmpDir + File.separator + new File(path).getName()
    }

    /**
     * Copy a global library script into a temporary location and make it
     * executable.
     *
     * @param srcPath The source path to the file.
     * @param destPath Where to put the file, defaulting to null which
     *                 means to put it in a temporary location.
     * @return The path to the now executable file.
     */
    static String copyGlobalLibraryScript(String srcPath, String destPath = null) {

        destPath = destPath ? createTempLocation(srcPath)
        writeFile file: destPath, text: libraryResource(srcPath)
        sh "chmod +x ${destPath}"
        return destPath
    }

    /**
     * Take a script from the resource directory of this library, copy it
     * to a temporary location, and execute it, parsing the output as JSON.
     *
     * @param scriptName The name of the script in the resource directory.
     * @return A Map object of the parsed JSON.
     */
    static Map callAndReturnJson(String scriptName) {
        def jsonOutput = ""

        try {

           // Read the file and place it in the workspace as an executable.
            script = copyGlobalLibraryScript(scriptName)

            // Run it, returning the output.
            jsonOutput = sh(
                script: script,
                returnStdout: true
            ).trim()

            // Parse it
            def parsedJson = readJSON text: jsonOutput

            return parsedJson
        } catch (Exception e) {
            error("Failed to parse JSON: ${e.message}. Raw output: ${jsonOutput}")
        }
    }
}
