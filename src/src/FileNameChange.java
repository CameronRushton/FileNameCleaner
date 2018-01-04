package src;

import java.io.File;
import java.io.IOException;

public class FileNameChange {
	private static String rootPath = "/Users/Cameron/Desktop/FileRenameTest";
	private int errors = 0;
	
	public static void main(String[] args) throws IOException {
		File root = new File(rootPath);
		FileNameChange c = new FileNameChange();
	    c.visitAllDirsAndFiles(root);
	    System.out.println("Finished with " + c.getNumErrors() + " errors");

	}
	
	public int getNumErrors() {
		return errors;
	}
	
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
		   System.out.println("path: " + path);
		   
		   String extension = this.getFileExtension(dir);
		   System.out.println("extension: " + extension);
		   
		   //Check for any words that contain these words. (upper or lower case)
		   String[] keywords = {"Audio", "Video", "Youtube", "Soundcloud", "Download", "Official", "Lyric", "HQ",
				   "Free", "Zippy", "kbps", "Monstercat", "HD"};
		   
		   /**
		    * trim/remove bits
		    */
		   if (newName.contains("&amp;")) newName = newName.replaceAll("&amp;", "&");
		   StringBuffer newNameBuffer = new StringBuffer(newName);
		   /**
		    * scan the string for brackets.
		    * if first or last char is a bracket, leave it
		    * otherwise add a space before or after the bracket depending on type (open or close)
		    * any extra spaces will be removed when split into words
		    *
		   for (int i = 1; i < newNameBuffer.length()-1; i++) {
			   if (newNameBuffer.charAt(i) == '(' || newNameBuffer.charAt(i) == '{' || newNameBuffer.charAt(i) == '[') {
				   newNameBuffer.insert(i, " ");
				   i+=2;
				   
			   } else if (newNameBuffer.charAt(i) == ')' || newNameBuffer.charAt(i) == '}' || newNameBuffer.charAt(i) == ']') {
				   newNameBuffer.insert(i+1, " ");
			   }
		   }
		   System.out.println("Spaced out: " + newNameBuffer.toString());*/
		   /**
		    * if a keyword is inside parenthesis, remove entire parenthesis
		    */
		   int prevPos = 0;
		   for (String keyword : keywords) {
			   pos = newNameBuffer.indexOf(keyword, prevPos);
			   
			   while (pos != -1) { //while there are keywords found, drop nukes
				   System.out.println("Keyword found");
				   newNameBuffer = nukeKeyword(newNameBuffer, keyword, pos);
				   prevPos = pos;
				   pos = newNameBuffer.indexOf(keyword, prevPos+1);
				   
			   }
		   }
		   String[] words = this.splitIntoWords(newNameBuffer.toString());
		   System.out.print("Split words: ");
		   for (String word : words) {
			   System.out.print(word + " ");
		   }
		   System.out.println();
		   
		   if (words.length > 1 && words[words.length-1].contains("-")) { //if the last word contains a hyphen 
			   newName = this.cutTail(words, newNameBuffer.toString());
			   System.out.println("Cut tail: " + newName);
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
		   newName = newNameBuffer.toString();
		   newName = path + newName;
		   System.out.println("Renamed to: " + newName);
		   //rename
		   File newFile = new File(newName);
 	 	   dir.renameTo(newFile);
	   }
		   
	}
	private String getRawPath(File dir) {
		return dir.toString().replace(dir.getName(), ""); //remove the file name to leave the containing path
	}
	
	private String getFileExtension(File dir) {
		return dir.getName().substring(dir.getName().lastIndexOf("."), dir.getName().length());
	}
	
	private String[] splitIntoWords(String sentence) {
		//NOTE: There could be a scenario where the spaces are replaced with underscores.
		//Even with a name such as Artist Song and splitting this by an underscore, it shouldn't matter since having two words is fine. 
		//(unless everything is removed by a keyword.
		sentence.replaceAll("\\s+", ""); //remove extra spaces between words
		if (sentence.split(" ").length <= 2) {
			return sentence.split("_");
		} else {
			return sentence.split(" ");
		}
		   
	}
	
	private StringBuffer nukeKeyword(StringBuffer newNameBuffer, String keyword, int pos) {
		System.out.println("Keyword: " + keyword);
		/**
		 * scan outwards from position (pos) on one side and look for the bracket facing the correct way
		 * (since it is assumed that parentheses have open and close brackets).
		 * If found correctly, the keyword is inside parenthesis and everything inside should be removed.
		 * If the keyword is not in parenthesis, just remove the keyword.
		 */
		//scanning right
		for (int i = pos; i < newNameBuffer.length(); i++) {
			if (newNameBuffer.charAt(i) == '(' || newNameBuffer.charAt(i) == '[' || newNameBuffer.charAt(i) == '{') {
				System.out.println("Found keyword not inside parenthesis");
				break; //Keyword is not inside parenthesis
			}
			else if (newNameBuffer.charAt(i) == ')' || newNameBuffer.charAt(i) == ']' || newNameBuffer.charAt(i) == '}') {
				//nuke keyword - is in parenthesis
				newNameBuffer = newNameBuffer.delete(pos, i+1); //delete(startInclusive, endExclusive) Delete from pos to right
				System.out.println("Buffer after right nuke: " + newNameBuffer.toString());
				
				//find index of open bracket
				for (int j = pos-1; j >= 0; j--) {
					if (newNameBuffer.charAt(j) == '(' || newNameBuffer.charAt(j) == '[' || newNameBuffer.charAt(j) == '{') {
						newNameBuffer = newNameBuffer.delete(j, pos); //delete(startInclusive, endExclusive) Delete from pos-1 to left
						System.out.println("Buffer after left nuke: " + newNameBuffer.toString());
						break;
					}
					if (j == 0)
						System.err.println("WARNING: Found keyword inside close parenthesis with no open parenthesis.\n"
								+ "Only right side nuked -Result: " + newNameBuffer.toString());
					
				}
				return newNameBuffer;
				
			}	
			
		}
		//delete keyword
		return new StringBuffer(newNameBuffer.toString().replaceFirst(keyword, ""));
	}
	
	private String cutTail(String[] words, String fileName) {
		//trim end bit after hyphen (residual youtube-dl code). Only do this if there is a hyphen in the last word
		int pos = 0;
		
		/**
		* snip end bit at hyphen (hyphen also gets removed)
		* keep snipping until the hyphen in question is one we dont want to remove or there are no more
		* NOTE: This assumes that there will be no names that have relevant data immediately after a hyphen
		* ex: Artist - Song will be fine, but Artist-Song will be cut short to Artist
		*/
			 
	    	do {
	    		try {
	    	    		pos = fileName.lastIndexOf("-");
	    	    		
	    	    	 	if (fileName.charAt(pos+1) == ' ') break; //break if the hyphen being examined has a space after it
	    	    	 		
	    	    	} catch(StringIndexOutOfBoundsException e) {
	    	    		System.err.println("ERROR on file: " + fileName); e.printStackTrace();
	    	    		errors++;
	    	    	}
	    	    	fileName = fileName.substring(0, pos);
	    	    	  	
	    	} while (fileName.contains("-"));
	    	 	  
	       
	    	return fileName;
		   
	}
	
}

