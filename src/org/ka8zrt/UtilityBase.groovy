package org.ka8zrt

import groovy.transform.CompileStatic

/**
 * A base utility class providing what will likely be some common helper
 * methods.
 */
@CompileStatic
class UtilityBase {

    /**
     * Copy a global library script into a temporary location and make it
     * executable.
     *
     * @param resourcePath The resource path to the file.
     * @param destPath Where to put the file, defaulting to null which
     *                 means to put it in a temporary location.
     * @return The path to the now executable file.
     */
    static String copyGlobalLibraryScript(String resourcePath, String destPath = null) {
        String fileName = new File(resourcePath).getName()
        String destFile = ''

        if (destPath == null) {
            String tmpDir = pwd(tmp: true)
            destFile = "${tmpDir}/${fileName}"
        } else {
            destFile = destPath
        }

        // Create the directory if needed, and write the library content.
        String content = libraryResource(resourcePath)
        writeFile(file: destFile, text: content)
        sh "chmod +x ${destFile}"

        return destFile
    }

    /**
     * Take a script from the resource directory of this library, copy it
     * to a temporary location, and execute it, parsing the output as JSON.
     *
     * @param scriptName The name of the script in the resource directory.
     * @return A Map object of the parsed JSON.
     */
    Map callAndReturnJson(String scriptName) {
        String jsonOutput = ''

        try {
            // Read the file and place it in the workspace as an executable.
            script = copyGlobalLibraryScript(scriptName)

            // Run it, returning the output.
            jsonOutput = sh(
                script: script,
                returnStdout: true
            ).trim()

            // Parse it
            Map parsedJson = readJSON(text: jsonOutput)

            return parsedJson
        } catch (e) {
            error("Failed to parse JSON: ${e.message}. Raw output: ${jsonOutput}")
        }
    }

}
