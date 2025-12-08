import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/** 
 * The class <code>TextFileHandler</code>
 *
 * @author Alexander Deutloff
 * @version 2.0.1, 2025-05-23
 */
public class TextFileHandler {
	private boolean utf8 = true;
	private boolean fileIsInClasspath = false;
	private String path;

	public TextFileHandler(String path) {
		this.path = path;
	}

	public TextFileHandler(String path, boolean fileIsInClasspath) {
		this.path = path;
		this.fileIsInClasspath = fileIsInClasspath;
	}

	public boolean isUtf8() {
		return utf8;
	}

	public void setUtf8(boolean utf8) {
		this.utf8 = utf8;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public File getFile() {			
		return new File(path);
	}

	public boolean isFileExisting() {
		return new File(path).exists();
	}

	public boolean isFileIsInClasspath() {
		return fileIsInClasspath;
	}

	public void setFileIsInClasspath(boolean fileIsInClasspath) {
		this.fileIsInClasspath = fileIsInClasspath;
	}

	//Funktioniert nur, falls sich die Datei nicht in einem JAR befindet
	public boolean deleteFile() {
		boolean deleted=false;

		File file = new File(path);
		if(file.isDirectory()) {
			deleted = deleteTree(file);
		}
		else {
			deleted = file.delete();
		}

		return deleted;
	}

	//Funktioniert nur, falls sich die Datei nicht in einem JAR befindet
	private boolean deleteTree( File directory )  {
		boolean deleted = false;

		//Erst müssen alle Dateien eines Ordners gel�scht werden, bevor der Ordner selbst gel�scht werden kann.
		for ( File file : directory.listFiles() ){
			if ( file.isDirectory() ) {
				deleteTree( file );
			}
			file.delete(); 
		}

		deleted =  directory.delete();

		return deleted;
	}

	public void append(StringBuffer pText) throws IOException{
		save(pText.toString(), true);
	}

	public void append(String pText) throws IOException{
		save(pText, true);
	}

	public void save(StringBuffer pText) throws IOException{
		save(pText.toString(), false);
	}

	public void save(String pText) throws IOException{
		save(pText, false);
	}

	public void save(String pText, boolean append) throws IOException{
		if(!fileIsInClasspath) {
			File file = new File(path);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();  // creates directories if not existing
			}
			
//			try (BufferedWriter bWriter = new BufferedWriter(new FileWriter(path, append))){	// try-with-resources statement to automatically close the declared resource 
			try (BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, append), utf8 ? StandardCharsets.UTF_8 : Charset.defaultCharset()))) {
				bWriter.write(pText);
			} 
		}
		else {
			throw new IOException("Saving to files in the classpath is not supported.");
		}
	}

	public BufferedReader loadToBufferedReader() throws FileNotFoundException, UnsupportedEncodingException, ResourceNotFoundException   {
		BufferedReader bReader = null;

		if(fileIsInClasspath) {
			/* getClass().getResourceAsStream(path):
			 * ------------------------------------
			 * Ein führender Slash ("/") in der Pfadangabe, z. B. ("/de/df/pwdmanager/resources/test.txt") weist Java an, 
			 * den Pfad absolut zu interpretieren, relativ zum "Root" des Klassenpfads.  
			 * Dies funktioniert in einem modularen Projekt **nicht**, wenn die Ressource in einem anderen Modul liegt, 
			 * es sei denn, dieses Modul ist entsprechend geöffnet.
			 * 
			 * Thread.currentThread().getContextClassLoader().getResourceAsStream(path):
			 * -------------------------------------------------------------------------
			 * Beim Zugriff über den ClassLoader (getClassLoader().getResourceAsStream()) darf der Pfad nicht mit einem führenden 
			 * Slash ("/") beginnen, um sicherzustellen, dass die Ressource korrekt relativ zum Klassenpfad gefunden wird.
			 * Liegt die Ressource im Klassenpfad eines anderen Moduls, muss dieses Modul explizit mittels `opens` geöffnet werden 
			 * (z.B. `opens de.df.pwdmanager.resources;`), um dem Zugriff zu ermöglichen.
			 */

			InputStreamReader inputStreamReader;
			InputStream inputStream = null;
			String relative_path;
			String absolute_path;

			if(path.startsWith("/")) {
				relative_path = path.substring(1, path.length());
				absolute_path = path;
			}
			else {
				relative_path = path;
				absolute_path = "/" + path;
			}

			// Versuch, die Ressource relativ zum Klassenpfad mit dem Class Loader zu laden:
			inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(relative_path);

			// Bei einem Fehlschlag: Versuch auf die Ressource über diese Klasse mit einer absoluten Pfadangabe, relativ zum "Root" des Klassenpfads zuzugreifen:
			if(inputStream==null) {
				inputStream = getClass().getResourceAsStream(absolute_path);
				if(inputStream==null) {
					throw new ResourceNotFoundException("\nThread.currentThread().getContextClassLoader().getResourceAsStream("+relative_path+") returns null\n"
							+ "getClass().getResourceAsStream("+absolute_path+") returns null");
				}
			}

			// Wenn inputStream null ist, wurde bereits eine Exception geworfen.
			if(utf8) {
				inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
			}
			else {
				inputStreamReader = new InputStreamReader(inputStream);
			}
			bReader = new BufferedReader(inputStreamReader);
		}
		else {
			if(utf8) {
				bReader = new BufferedReader(new InputStreamReader( new FileInputStream(path),StandardCharsets.UTF_8) );
			}
			else {
				bReader = new BufferedReader(new InputStreamReader( new FileInputStream(path)) );
			}
		}

		return bReader;
	}

	public List<String> loadToStringArray() throws FileNotFoundException, UnsupportedEncodingException, IOException, ResourceNotFoundException{
		List<String> lines = new ArrayList<String>();
		String line;

		try (BufferedReader bReader = loadToBufferedReader()){ // try-with-resources statement to automatically close the declared resource
			while ((line = bReader.readLine()) != null) {
				lines.add(line);
			}
		}

		return lines;
	}

	public StringBuilder loadToStringBuilder() throws FileNotFoundException, UnsupportedEncodingException, IOException, ResourceNotFoundException  {
		StringBuilder stringBuilder = new StringBuilder();
		String line;

		try (BufferedReader bReader = loadToBufferedReader()){ // try-with-resources statement to automatically close the declared resource
			while ((line = bReader.readLine()) != null) {
				stringBuilder.append(line + System.getProperty("line.separator"));
			}
		}

		return stringBuilder;
	}

	public StringBuffer loadToStringBuffer() throws FileNotFoundException, UnsupportedEncodingException, IOException, ResourceNotFoundException {
		StringBuffer sBuffer = new StringBuffer();
		String line;

		try (BufferedReader bReader = loadToBufferedReader()){ // try-with-resources statement to automatically close the declared resource
			while ((line = bReader.readLine()) != null) {
				sBuffer.append(line + System.getProperty("line.separator"));
			}
		}

		return sBuffer;
	}

	public void copyTo(String destinationPath) throws FileNotFoundException, IOException {
		File source = new File(path);
		File destination = new File(destinationPath);

		try ( // try-with-resources statement to automatically close the declared resource
				FileInputStream fis = new FileInputStream(source); 
				FileOutputStream fos = new FileOutputStream(destination);
				FileChannel inChannel = fis.getChannel();
				FileChannel outChannel = fos.getChannel();	
				) {

			inChannel.transferTo(0, inChannel.size(), outChannel);
		}
	}

	public void clearTextFile() throws IOException{
		save("");
	}

}
