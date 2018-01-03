package src;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FileNameChange {
	private static String rootPath = "/Volumes/MUSIC/Old mp3s";
	private String path;
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
	   //System.out.println("Current directory: " + dir);
	   
	   if (dir.isDirectory()) {
	      String[] children = dir.list();
	      
	      for (int i = 0; i < children.length; i++) {
	         visitAllDirsAndFiles(new File(dir, children[i])); 
	      } 
	   } 
	   else if (dir.isFile() && dir.getName().charAt(0) != '.') { //it's a file, so rename. Ignore .DS_Store & hidden files
		   String newName = "";
		   String extension;
		   int pos;
		   System.out.println(dir.getName());
		   path = dir.toString().replace(dir.getName(), ""); //remove the file name to leave the containing path
		   
		   //get extension
		   pos = dir.getName().lastIndexOf(".");
		   extension = dir.getName().substring(pos, dir.getName().length());
		   
		   //Check for any words that contain these words. (upper or lower case)
		   String[] keywords = {"Audio", "Video", "Youtube", "Soundcloud", "Download", "Official", "Lyric", "HQ",
				   "Free", "Zippy", "kbps", "Monstercat", "HD"};
		   
		   /**
		    * trim/remove bits
		    */
		   String[] words = dir.getName().split(" ");
		   
		   //NOTE: There could be a scenario where the spaces are replaced with underscores.
		   if (words.length <= 2) { //Even with a name such as Artist Song and splitting this by an underscore, it
			   						// ...shouldn't matter since having two words is fine. (unless everything is
			   						// ...removed by a keyword.
			   words = dir.getName().split("_");
			   //System.out.println("Splitting by _");
		   }
		   
		   ArrayList<String> fileNameWords = new ArrayList<String>();
		   boolean skipWord = false;
		   
		   //remove anything with a keyword in it
		   for (String word : words) {
			   if (word.contains("&amp;")) word = word.replace("&amp;", "&");
			   
			   for (String keyword : keywords) {
				   if (word.toLowerCase().contains(keyword.toLowerCase())) {
					   //dont add word to arraylist for reconstruction
					   skipWord = true;
				   }   
			   }
			   if (!skipWord) fileNameWords.add(word);
				   
			   skipWord = false;
			   
		   }
		   //reconstruct the file name
		   String fileName = "";
		   for (int i = 0; i < fileNameWords.size()-1; i++) {
			   fileName += fileNameWords.get(i);
			   fileName += " ";
		   }
		   //add the last word with the extension
		   //TODO: Program crashes here when it encounters a name consisting of only one word after stripping
		   if (fileNameWords.size() > 1)
			   fileName += fileNameWords.get(fileNameWords.size()-1);
		   
		   //then add it to newName with path included
		   newName = path + fileName;
		   
		   //remove any old [www.mp3fiber.com] & other watermarks
		   if (newName.contains("[www.mp3fiber.com]"))
			   newName = newName.replace("[www.mp3fiber.com]", ""); 
		   if (newName.contains("[www.MP3Fiber.com]"))
			   newName = newName.replace("[www.MP3Fiber.com]", "");
		   if (newName.contains("[Monstercat Release]"))
			   newName = newName.replace("[Monstercat Release]", "");
		   if (newName.contains("(OUT NOW!)"))
			   newName = newName.replace("(OUT NOW!)", "");
		   if (newName.contains("[OUT NOW!]"))
			   newName = newName.replace("[OUT NOW!]", "");
		   //trim end bit after hyphen (residual youtube-dl code). Only do this if there is a hyphen in the last word
		   if (fileNameWords.size() > 1 && fileNameWords.get(fileNameWords.size()-1).contains("-")) { //if the last word contains a hyphen  
			  
			  /**
			   * snip end bit at hyphen (hyphen also gets removed)
			   * keep snipping until the hyphen in question is one we dont want to remove or there are no more
			   * NOTE: This assumes that there will be no names that have relevant data immediately after a hyphen
			   * ex: Artist - Song will be fine, but Artist-Song will be cut short to Artist
			   */
			 
	    	      do {
	    	    	  	try {
	    	    	  		pos = newName.lastIndexOf("-");
	    	    	  		if (newName.charAt(pos+1) == ' ') break; //break if the hyphen being examined has a space after it
	    	    	  	} catch(StringIndexOutOfBoundsException e) {
	    	    	  		System.out.println("ERROR on file: " + newName); e.printStackTrace();
	    	    	  		errors++;
	    	    	  	}
	    	    	  	newName = newName.substring(0, pos);
	    	    	  	
	    	      } while (newName.contains("-"));
	    	 	  
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
		   //rename
		   File newFile = new File(newName);
 	 	   dir.renameTo(newFile);
		   
	   }
	}
}
