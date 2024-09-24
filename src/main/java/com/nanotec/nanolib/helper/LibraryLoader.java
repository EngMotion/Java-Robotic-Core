package com.nanotec.nanolib.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;

import com.nanotec.nanolib.NanoLibAccessor;
import com.nanotec.nanolib.Nanolib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The library loader serves as a helper class to load nanolib 
 * library or first extract the dlls from the jar and then load them 
 * afterwards.
 */
public final class LibraryLoader {

	private static final Logger log = LoggerFactory.getLogger(LibraryLoader.class);

	private LibraryLoader() {
	}

	/**
	* Extracts dll from .jar archive to the specified directory
	* and loads it
	* 
	* @param	file 		- name of the file to extract
	* @param	targetDir	- target directory to extract the file to
	*
	* @throws IOException
	*/
	private static void loadFromJar(String file, Path targetDir) throws IOException {
		// extends name with .dll, .so or .dylib
		String fileName = System.mapLibraryName(file);
		//Get system type and arch
		String os = System.getProperty("os.name").toLowerCase();
		String osPath = "win";
		if (os.contains("mac")) {
			osPath = "mac";
		} else if (os.contains("nix") || os.contains("nux")) {
			osPath = "linux";
		}
		String arch = System.getProperty("os.arch").toLowerCase();
		//if arm
		if (arch.contains("arm")) {
			osPath += "_arm64";
		}
		String resourceName = "/nanolib/" + osPath+ "/" + fileName;
		File tempFile = new File(targetDir.toString() + File.separator + fileName);
		// find lib in the jar
		try (InputStream inStream = Nanolib.class.getResourceAsStream(resourceName)) {
			// copy library to tempFile
			try (FileOutputStream outStream = new FileOutputStream(tempFile)) {
				
				int 	bytesRead;
				byte[] 	buffer = new byte[8192];
				
				while ((bytesRead = inStream.read(buffer)) > 0) {
					outStream.write(buffer, 0, bytesRead);
				}
			}
			catch (Exception e) {
				throw new IOException(e);
			}
		} catch (Exception e) {
			throw new IOException("Could not find " + resourceName + " in the jar file", e);
		}
		
		System.load(tempFile.toString());
	}

	/**
	 * Extracts / loads required dlls and returns accessor
	 *
	 * @return NanoLibAccessor
	 * @throws IOException
	 */
	public static NanoLibAccessor setup() throws IOException {
		// try to load library directly
		// if this fails, try to load from jar

		Path tempDir = Files.createTempDirectory("Nanolib");
		String libName = "nanolib_java";
		try {
			String osLibName = System.mapLibraryName(libName);
			System.loadLibrary(osLibName);
		} catch (UnsatisfiedLinkError e) {
			loadFromJar(libName, tempDir);
		}

		libName = "nanolibm_canopen";
		try {
			String osLibName = System.mapLibraryName(libName);
			System.loadLibrary(osLibName);
		} catch (UnsatisfiedLinkError e) {
			loadFromJar(libName, tempDir);
		}

		libName = "nanolibm_ethercat";
		try {
			String osLibName = System.mapLibraryName(libName);
			System.loadLibrary(osLibName);
		} catch (UnsatisfiedLinkError e) {
			loadFromJar(libName, tempDir);
		}

		libName = "nanolibm_modbus";
		try {
			String osLibName = System.mapLibraryName(libName);
			System.loadLibrary(osLibName);
		} catch (UnsatisfiedLinkError e) {
			loadFromJar(libName, tempDir);
		}

		libName = "nanolibm_restful-api";
		try {
			String osLibName = System.mapLibraryName(libName);
			System.loadLibrary(osLibName);
		} catch (UnsatisfiedLinkError e) {
			loadFromJar(libName, tempDir);
		}

		libName = "nanolibm_usbmsc";
		try {
			String osLibName = System.mapLibraryName(libName);
			System.loadLibrary(osLibName);
		} catch (UnsatisfiedLinkError e) {
			loadFromJar(libName, tempDir);
		}

		// before accessing the nanolib, the pointer to the accessor class
		// needs to be created and stored somewhere
		return Nanolib.getNanoLibAccessor();
	}
}
