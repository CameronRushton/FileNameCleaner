package src;

import java.io.File;
import java.io.IOException;

public class FileNameChange {
	
	//Root path
	private static String rootPath = "/Users/Cameron/Desktop/FileRenameTest";
	//Number of errors
	private int errors = 0;
	
	public static void main(String[] args) throws IOException {
		
		File root = new File(rootPath);
		
		FileNameChange c = new FileNameChange();
	    c.visitAllDirsAndFiles(root);
	    
	    System.out.println("Finished with " + c.getNumErrors() + " errors");
	}
	
	/**
	 * @return int - Number of errors
	 */
	public int getNumErrors() {
		return errors;
	}
	
	/**
	 * Scan through files and directories and change file names to get rid of keywords and other imperfections
	 * @param dir : File
	 * @throws IOException
	 */
	public void visitAllDirsAndFiles(File dir) throws IOException {
	   System.out.println("Current directory: " + dir);
	   
	   if (dir.isDirectory()) {
	      String[] children = dir.list();
	      
	      for (int i = 0; i < children.length; i++) {
	         visitAllDirsAndFiles(new File(dir, children[i])); 
	      }
	      
	   } else if (dir.isFile() && dir.getName().charAt(0) != '.') { //it's a file, so rename. Ignore .DS_Store & hidden files
		   
		   String newName = dir.getName();
		   int pos;
		   String path = this.getRawPath(dir);
		   String extension = this.getFileExtension(dir);
		   
		   //Check for any words that contain these words. (upper or lower case)
		   String[] keywords = {"Audio", "Video", "Youtube", "Soundcloud", "Download", "Official", "Lyric", "HQ",
				   "Free", "Zippy", "kbps", "Monstercat", "HD", "Cover Art", "Premiere"};
		   
		   /**
		    * trim/remove bits
		    */
		   if (newName.contains("&amp;")) newName = newName.replaceAll("&amp;", "&");
		   //TODO: The following makes the last word break up when theres an underscore in it.
		   //Ex: Artist_-_Song_(Original_Mix)-XX_XX-X.flac -> Artist - Song (Original Mix)-XX XX
		   if (newName.contains("_")) newName = newName.replaceAll("_", " ");

		   /**
		    * if a keyword is inside parenthesis, remove entire parenthesis
		    */
		   for (String keyword : keywords) {
			   pos = newName.toLowerCase().indexOf(keyword.toLowerCase(), 0);
			   
			   while (pos != -1) { //while there are still more of the same keywords found, drop nukes
				   newName = nukeKeyword(newName, keyword, pos);
				   pos = newName.toLowerCase().indexOf(keyword.toLowerCase(), pos+1);
				   
			   }
		   }
		   
		   String[] words = this.splitIntoWords(newName);
		   if (words[words.length-1].contains("-")) { //if the last word contains a hyphen 
			   newName = this.cutTail(words, newName);
		   }
		   //if the extension got lost, add it
		   if (!newName.contains(extension)) {
			   newName += extension;
		   }
		   
		   //if there is a space before the extension, remove it
		   if (newName.charAt(newName.lastIndexOf(".")-1) == ' ') {
			   StringBuilder buildName = new StringBuilder(newName);
			   buildName.deleteCharAt(newName.lastIndexOf(".")-1);
			   newName = buildName.toString();
		   }
		   
		   //remove extra spaces
		   newName.replaceAll("\\s+", "");
		   newName = path + newName;

		   //rename
		   File newFile = new File(newName);
 	 	   dir.renameTo(newFile);
	   }
		   
	}
	/**
	 * Gets the raw path of the file so that changes can be made to the name
	 * @param dir : File
	 * @return String - The raw path without file name
	 */
	private String getRawPath(File dir) {
		return dir.toString().replace(dir.getName(), ""); //remove the file name to leave the containing path
	}
	
	/**
	 * Gets file extension by looking for the period in the file name
	 * @param dir : File
	 * @return String - The extension
	 */
	private String getFileExtension(File dir) {
		return dir.getName().substring(dir.getName().lastIndexOf("."), dir.getName().length());
	}
	
	/**
	 * Splits into words and eliminates extra spaces
	 * @param sentence : String
	 * @return String[] - the sentence's words
	 */
	private String[] splitIntoWords(String sentence) {
		sentence.replaceAll("\\s+", ""); //remove extra spaces between words
		return sentence.split(" ");  
	}
	
	/**
	 * Scan outwards from position (pos) on one side and look for the bracket facing the correct way
	 * (since it is assumed that parentheses have open and close brackets).
	 * If found correctly, the keyword is inside parenthesis and everything inside should be removed.
	 * If the keyword is not in parenthesis, just remove the keyword.
	 * @param newName : String
	 * @param keyword : String
	 * @param pos : int
	 * @return String - the edited file name
	 */
	private String nukeKeyword(String newName, String keyword, int pos) {
		StringBuffer newNameBuffer = new StringBuffer(newName);
		
		//scanning right
		for (int i = pos; i < newNameBuffer.length(); i++) {
			if (newNameBuffer.charAt(i) == '(' || newNameBuffer.charAt(i) == '[' || newNameBuffer.charAt(i) == '{') {
				break; //Keyword is not inside parenthesis
			}
			else if (newNameBuffer.charAt(i) == ')' || newNameBuffer.charAt(i) == ']' || newNameBuffer.charAt(i) == '}') {
				//nuke keyword - is in parenthesis
				newNameBuffer = newNameBuffer.delete(pos, i+1); //delete(startInclusive, endExclusive) Delete from pos to right
				
				//find index of open bracket
				for (int j = pos-1; j >= 0; j--) {
					if (newNameBuffer.charAt(j) == '(' || newNameBuffer.charAt(j) == '[' || newNameBuffer.charAt(j) == '{') {
						newNameBuffer = newNameBuffer.delete(j, pos); //delete(startInclusive, endExclusive) Delete from pos-1 to left
						break;
					}
					if (j == 0) {
						System.err.println("WARNING: Found keyword inside close parenthesis with no open parenthesis.\n"
								+ "Only right side nuked -Result: " + newNameBuffer.toString());
						errors++;
					}
					
				}
				return newNameBuffer.toString();
				
			}	
			
		}
		
		//delete keyword Note: This will delete all occurrences (not just whole words - add \\b * for that after keyword
	    return newName.replaceAll("(?i)" + keyword, "").trim(); //elim leading and trailing white space with trim
	}
	
	/**
	* Snip end bit at hyphen (hyphen also gets removed)
	* keep snipping until the hyphens in the last word are gone.
	* Note: important data may be deleted if the last word contains a hyphen before data.
	* Ex: Artist-(Original)-XXXXX.flac -> Artist.flac (Should be: Artist-(Original).flac)
	* @param words : String[]
	* @param fileName : String
	* @return String - the edited file name
	*/
	private String cutTail(String[] words, String fileName) {
		//trim end bit after hyphen (residual youtube-dl code). Only do this if there is a hyphen in the last word
		int pos = 0;
		String lastWord = words[words.length-1];
			 
	    	do {
	    		pos = fileName.lastIndexOf("-");
	    	    	fileName = fileName.substring(0, pos);
	    	    	lastWord = lastWord.substring(0, lastWord.lastIndexOf('-'));
	    	    	  	
	    	} while (lastWord.contains("-")); //while the last word contains a hyphen

	    	return fileName;
		   
	}
}
