// Project CSI2120/CSI2520
// Winter 2025
// Robert Laganiere, uottawa.ca

package org.example;

import java.io.*;
import java.util.ArrayList;

// this is the class that will generate the recommendations for a user
public class RecommendationEngine {

	public ArrayList<Movie> moviesDatabase; //Create a database with all the movies

	public void readMovies(String csvFile) throws IOException,
			NumberFormatException {

		String line;
		String delimiter = ","; // Assuming values are separated by commas

		BufferedReader br = new BufferedReader(new FileReader(csvFile));
		// Read each line from the CSV file
		line = br.readLine();

		while ((line = br.readLine()) != null && line.length() > 0) {
			// Split the line into parts using the delimiter
			String[] parts = line.split(delimiter);
			String title;

			// parse the ID
			int movieID= Integer.parseInt(parts[0]);

			if (parts.length < 3)
				throw new NumberFormatException("Error: Invalid line structure: " + line);

			// we assume that the first part is the ID
			// and the last one are genres, the rest is the title
			title= parts[1];
			if (parts.length > 3) {

				for (int i=2; i<parts.length-1; i++)
					title+= parts[i];
			}

			moviesDatabase.add(new Movie(movieID,title));
		}

	}

	public static void main(String[] args) {

		//Need to call the functions to read the movies and rating files here
		
		//to modify
		try {
			//How to run the code: java RecommendationEngine 44 movies.csv ratings.csv
			RecommendationEngine rec= new RecommendationEngine();
			rec.readMovies(args[1]);

		    // just printing few movies
			for (int i=0; i<20; i++) {
				
				System.out.println(rec.moviesDatabase.get(i).getTitle().toString());
			}
			
        } catch (Exception e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
	}
}
