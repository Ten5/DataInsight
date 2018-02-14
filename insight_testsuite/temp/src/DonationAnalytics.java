import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;

public class DonationAnalytics {

	// Stores the name of the file to be written on.
	private String outputFile;
	
	// Stores the input percentile value.
	private int percentile;
	
	// Stores the input data.
	private ArrayList<InputNode> inputData;
	
	// Stores the list of repeating donors in the form of a HashMap.
	private HashMap<String, String> repeatList;
	
	// Stores the data the repeating donors contribute to.
	private HashMap<String, ArrayList<RecipientNode>> repeatDetailList;
	
	public DonationAnalytics(String outputFile) {
		this.outputFile = outputFile;
		this.percentile = 0;
		this.inputData = new ArrayList<InputNode>();
		repeatList = new HashMap<String, String>();
		repeatDetailList = new HashMap<String, ArrayList<RecipientNode>>();
	}
	
	/* Reads from the file whose File Name is passed as parameter.
	 * FileType specifies whether it contains the percentile value or actual text. */
	public void inputReader(String fileName, int fileType) {
		
		// Read from the file.
		FileReader fr = null;
		try {
			fr = new FileReader(fileName);
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
		BufferedReader br = new BufferedReader(fr);
		try {
			if(fileType == 1) {
				this.percentile = Integer.parseInt(br.readLine());
			}
			
			if(fileType == 2) {
				String str = br.readLine();
				String st[] = null;
				while(str != null) {
					st = str.split(Pattern.quote("|"));
					
					String cmte = st[0];
					if(cmte.equals("")) {
						str = br.readLine();
						continue;
					}
					
					String name = st[7];
					if(name.equals("")) {
						str = br.readLine();
						continue;
					}
					
					String zipCode =  st[10];
					if(zipCode.length() < 5) {
						str = br.readLine();
						continue;
					}
					zipCode = zipCode.substring(0, 5);
					
					String transDt = st[13];
					if(transDt.length() < 8) {
						str = br.readLine();
						continue;
					}
					
					double transAmt = Double.parseDouble(st[14]);
					
					String other = st[15];
					if(!other.equals("")) {
						str = br.readLine();
						continue;
					}
					
					InputNode newNode = new InputNode(cmte, name, zipCode, transDt, transAmt, other);
					this.inputData.add(newNode);
					
					str = br.readLine();
				}
			}
			
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	/* Calculates the percentile of a list from the given size
	 * and the percentile value of the class.
	 * Returns a long value. */
	public long calculatePercentile(int size) {
		
		double ordinalRank = this.percentile * size / 100;  
		return Math.round(Math.ceil(ordinalRank));
	}
	
	/* Processes the input data as a stream. 
	 * Stores the processed data and uses it to find repeating donors
	 * as well as the recipients and the contributions made. */
	public void processData() {
		
		for(InputNode in : this.inputData) {
			
			String key = in.getName() + in.getZipCode();
			
			// Repeating donor found.
			if(repeatList.containsKey(key)) {
				String value = repeatList.get(key);
				String prevYearValue = value.substring(value.length() - 4);
				
				String year = in.getTransactionDate().substring(in.getTransactionDate().length() - 4);
				String innerKey = in.getCmteID() + in.getZipCode() + year;
				
				// Checking if the repeat donor has contribution from a previous year.
				if((Integer.parseInt(year)) < (Integer.parseInt(prevYearValue))) {
					continue;
				}
				
				// Recipient with same zip and same year found.
				if(repeatDetailList.containsKey(innerKey)) {
					ArrayList<RecipientNode> newArr = repeatDetailList.get(innerKey);
					newArr.add(new RecipientNode(in.getCmteID(), year, in.getZipCode(), Math.round(in.getTransactionAmount())));
					
					Collections.sort(newArr);
					repeatDetailList.put(innerKey, newArr);
					outputData(newArr);
				}
				// Recipient with same zip and same year not found.
				else {
					ArrayList<RecipientNode> newArr = new ArrayList<RecipientNode>();
					newArr.add(new RecipientNode(in.getCmteID(), year, in.getZipCode(), Math.round(in.getTransactionAmount())));
					
					repeatDetailList.put(innerKey, newArr);
					outputData(newArr);
				}
			}
			// Repeating donor not found.
			else {
				String year = in.getTransactionDate().substring(in.getTransactionDate().length() - 4);
				String value = in.getCmteID() + in.getZipCode() + year;
				repeatList.put(key, value);
			}
		}
	}
	
	/* Used to write the data sent in a list to the output file.
	 * Also finds the running percentile value as well as the total contribution made. */
	public void outputData(ArrayList<RecipientNode> list) {
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(outputFile, true);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		BufferedWriter bw = new BufferedWriter(fw);
		int currentPercentile = (int)calculatePercentile(list.size());
		long percentileValue = Math.round(list.get(currentPercentile > 0? (currentPercentile - 1) : 0).getContribution());
		
		long totalContribution = 0;
		int totalContributors = 0;
		String recipient = "";
		String year = "";
		String zipCode = "";
		
		for(RecipientNode nn : list) {
			year = nn.getYear();
			recipient = nn.getRecipient();
			zipCode = nn.getZipCode();
			totalContribution += Math.round(nn.getContribution());
			totalContributors++;
		}
		try {
			bw.write(recipient + "|" + zipCode + "|" + year + "|" + percentileValue + "|" + totalContribution + "|" + totalContributors);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void main(String args[]) {
		
		String inputFile1 = args[0];
		String inputFile2 = args[1];
		String outputFile = args[2];
		DonationAnalytics da = new DonationAnalytics(outputFile);
		da.inputReader(inputFile1, 1);
		da.inputReader(inputFile2, 2);
		da.processData();
	}
}

/* Stores the input data as nodes. */
class InputNode {
	
	private String cmteID;
	private String name;
	private String zipCode;
	private String transactionDate;
	private double transactionAmount;
	
	public InputNode (String cmteID, String name, String zipCode, String transactionDate, double transactionAmount, String otherID) {
		
		this.cmteID = cmteID;
		this.name = name;
		this.zipCode = zipCode;
		this.transactionDate = transactionDate;
		this.transactionAmount = transactionAmount;
	}
	
	public String getCmteID () {
		return this.cmteID;
	}
	
	public String getName () {
		return this.name;
	}
	
	public String getZipCode () {
		return this.zipCode;
	}
	
	public String getTransactionDate () {
		return this.transactionDate;
	}
	
	public double getTransactionAmount () {
		return this.transactionAmount;
	}
	
	public String display() {
		return cmteID + "|" + name + "|" + zipCode + "|" + transactionDate + "|" + transactionAmount;
	}
}


/* Stores the recipient data as nodes. */
class RecipientNode implements Comparable<RecipientNode> {
	
	private String recipient;
	private String year;
	private String zipcode;
	private long contributionAmt;
	
	public RecipientNode(String recipient, String year, String zipcode, long contributionAmt) {
		
		this.recipient = recipient;
		this.year = year;
		this.zipcode = zipcode;
		this.contributionAmt = contributionAmt;
	}
	
	public String getRecipient () {
		return this.recipient;
	}
	
	public String getYear() {
		return this.year;
	}
	
	public String getZipCode () {
		return this.zipcode;
	}
	
	public double getContribution () {
		return this.contributionAmt;
	}
	
	public String display() {
		return recipient + "|" + year + "|" + zipcode + "|" + contributionAmt;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        final RecipientNode other = (RecipientNode) o;
        String str1 = this.recipient + this.zipcode + this.year;
		String str2 = other.recipient + other.zipcode + other.year;
		
        if (!str1.equals(str2))
            return false;
        return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
        int result = 1;
        String str = "";
        if(recipient == null || zipcode == null || year == null) {
        	str = null;
        }
        else {
        	str = recipient + zipcode + year;
        }
        
        result = prime * result + ((str == null) ? 0 : str.hashCode());
        return result;
	}
	
	@Override
	public int compareTo(RecipientNode node) {
		return (this.contributionAmt > node.contributionAmt ? 1 : -1);
	}
}