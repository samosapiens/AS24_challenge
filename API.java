import java.util.*;
import java.util.stream.Collectors;
import java.io.*;
import java.text.*;

public class API {

    //-----------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------------
    //                                              CSV Files Parse Utils
    //-----------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------------

    private static List<String> getRecordFromLine(String line) {
        // From: https://www.baeldung.com/java-csv-file-array
        List<String> values = new ArrayList<String>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(",");
            while (rowScanner.hasNext()) values.add(rowScanner.next().replace("\"", "")); // Edited to remove quoates from string values
        }
        return values;
    }

    private static List<List<String>> readCSV(String filename) {
        // From: https://www.baeldung.com/java-csv-file-array
        List<List<String>> records = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(filename));) {
            while (scanner.hasNextLine()) records.add(getRecordFromLine(scanner.nextLine()));
        }
        catch (FileNotFoundException e) {
            System.out.println("Error: File " + filename + " not found!\n");
            e.printStackTrace();
        }
        return records;
    }

    //-----------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------------
    //                                          Seller Types Average Methods
    //-----------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------------

    private static Set<String> getSellerTypes(List<List<String>> listings) {
        Set<String> seller_types = new HashSet<String>();
        for (List<String> list : listings) seller_types.add(list.get(4));
        return seller_types;
    }

    private static Map<String, Float> getAverageSellingPrice(List<List<String>> listings, Set<String> seller_types) {
        Map<String, Float> average_selling_prices = new HashMap<String, Float>();
        for (String st : seller_types) {
            int count = 0;
            float avg = 0f;
            for (List<String> list : listings) {
                if (list.get(4).equals(st)) {
                    avg += Float.parseFloat(list.get(2));
                    count++;
                }
            }
            avg /= (float) count;
            average_selling_prices.put(st, (float) Math.round(avg));
        }
        return average_selling_prices;
    }

    private static void printSellerTypesAverages(Map<String, Float> average_selling_prices) {
        System.out.println("\n*** Seller Types Averages:\n");
        System.out.println("|-------------|-----------------|");
        System.out.println("| Seller Type | Average in Euro |");
        System.out.println("|-------------|-----------------|");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        for (Map.Entry<String, Float> entry : average_selling_prices.entrySet()) {
            String currency_value = formatter.format(entry.getValue()).replaceAll("\\,00", ",-").replaceAll(" €", "");
            System.out.println(String.format("| %11s | %15s |", entry.getKey(), String.format("\u20AC %s", currency_value)));
            System.out.println("|-------------|-----------------|");
        }
    }

    //-----------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------------
    //                          Percentual Distribution of Available Cars by Make Methods
    //-----------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------------

    private static Set<String> getMakes(List<List<String>> listings) {
        Set<String> makes = new HashSet<String>();
        for (List<String> list : listings) makes.add(list.get(1));
        return makes;
    }

    private static Map<String, Float> getPercentualDistributionOfAvailableCars(List<List<String>> listings, Set<String> makes) {
        Map<String, Float> percentual_distribution_cars = new HashMap<String, Float>();
        for (String make : makes) {
            int count = 0;
            for (List<String> list : listings) {
                if (list.get(1).equals(make)) count++;
            }
            percentual_distribution_cars.put(make, (float) (100f * ((float) count/ (float) listings.size())) );
        }
        // Sort by value
        Map<String, Float> percentual_distribution_cars_sorted = sortByValue(percentual_distribution_cars);
        return percentual_distribution_cars_sorted;
    }

    private static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
        // From http://www.programmersheaven.com/download/49349/download.aspx
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Object>() {
            @SuppressWarnings("unchecked")
            public int compare(Object o1, Object o2) {
                // TODO: Sort by value and by key
                return ((Comparable<V>) ((Map.Entry<K, V>) (o2)).getValue()).compareTo(((Map.Entry<K, V>) (o1)).getValue());
            }
        });
        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext();) {
            Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    private static void printPercentualDistributionOfCarsByMake(Map<String, Float> percentual_distribution_cars) {
        System.out.println("\n*** Percentual Distribution of Cars by Make:\n");
        System.out.println("|---------------|--------------|");
        System.out.println(String.format("| %13s | %12s |", "Make", "Distribution"));
        System.out.println("|---------------|--------------|");
        for (Map.Entry<String, Float> entry : percentual_distribution_cars.entrySet()) {
            System.out.println(String.format("| %13s | %10.0f %% |", entry.getKey(), entry.getValue()));
            System.out.println("|---------------|--------------|");
        }
    }

    //-----------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------------
    //                              Average Price of the 30% Most Contacted Listnings
    //-----------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------------

    private static Map<String, Integer> getMostContactedListings(List<List<String>> listings, List<List<String>> contacts) {
        // Get frequency of contact for each listing id
        int freq;
        Map<String, Integer> frequencies = new HashMap<String, Integer>();
        for (List<String> list : listings) {
            freq = 0;
            for (List<String> contact : contacts) {
                if (contact.get(0).equals(list.get(0))) freq++;
            }
            frequencies.put(list.get(0), freq);
        }
        return sortByValue(frequencies);
    }

    private static float getAveragePriceOfMostContactedListings(List<List<String>> listings, Map<String, Integer> frequencies, float percentage) {
        // Calculate average for the given percentage of contacts
        float avg = 0f;
        int elems = 0, count = 0, limit = Math.round(frequencies.size()*percentage/100f);
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            avg += Float.parseFloat(listings.stream().filter(item -> item.get(0).equals(entry.getKey())).collect(Collectors.toList()).get(0).get(2));
            count += entry.getValue();
            elems ++;
            if (count >= limit) break;
        }
        return (float) Math.round(avg/((float) elems));
    }

    private static void printAveragePriceOfMostContactedListings(float avg) {
        System.out.println("\n*** Average Price of Most Contacted Listings:");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        String currency_value = formatter.format(avg).replaceAll("\\,00", ",-").replaceAll(" €", "");
        System.out.println(String.format("\n| Average Price |\n| %13s |", String.format("\u20AC %s", currency_value)));
    }

    //-----------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------------
    //                                     Top 5 Most Contacted Listings per Month
    //-----------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------------
    
    private static String getDateFromUTC(String utc) {
        Date date = new Date(Long.parseLong(utc));
        return String.format("%02d.%d", date.getMonth() + 1, date.getYear() + 1900);
    }

    private static List<List<String>> getMostContactedListingsByMonth(List<List<String>> listings, List<List<String>> contacts, String month, int ranking) {
        // Get frequency of contact for each listing id
        int freq;
        Map<String, Integer> frequencies = new HashMap<String, Integer>();
        for (List<String> list : listings) {
            freq = 0;
            for (List<String> contact : contacts) {
                if (getDateFromUTC(contact.get(1)).equals(month) && contact.get(0).equals(list.get(0))) freq++;
            }
            frequencies.put(list.get(0), freq);
        }
        frequencies = sortByValue(frequencies);
        // Generate ranking of most contacted listings by month
        int count = 0;
        List<List<String>> top = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            List<String> result = new ArrayList<String>();
            List<String> attribs = listings.stream().filter(item -> item.get(0).equals(entry.getKey())).collect(Collectors.toList()).get(0);
            result.add(String.format("%d", ++count));
            result.add(attribs.get(0));
            result.add(attribs.get(1));
            result.add(attribs.get(2));
            result.add(attribs.get(3));
            result.add(String.format("%d", entry.getValue()));
            top.add(result);
            if (count >= ranking) break;
        }
        return top;
    }

    private static Map<String, List<List<String>>> getTopMostContactedListingsPerMonth(List<List<String>> listings, List<List<String>> contacts, int ranking) {
        // Get keys of months in contacts
        TreeSet<String> months_set = new TreeSet<String>();
        for (List<String> contact : contacts) months_set.add(getDateFromUTC(contact.get(1)));
        // Create Map between keys and top lists
        Map<String, List<List<String>>> tops = new TreeMap<String, List<List<String>>>();
        for (String month : months_set) tops.put(month, getMostContactedListingsByMonth(listings, contacts, month, ranking));
        return tops;
    }

    private static void printTopMostContactedListingsPerMonth(Map<String, List<List<String>>> tops) {
        System.out.println("\n*** Top 5 most contacted listings per Month:");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        for (Map.Entry<String, List<List<String>>> entry : tops.entrySet()) {
            System.out.println(String.format("\nMonth: %s\n", entry.getKey()));
            System.out.println("|---------|------------|---------------|---------------|------------|--------------------------|");
            System.out.println("| Ranking | Listing ID |          Make | Selling Price |    Mileage | Total Amount of Contacts |");
            System.out.println("|---------|------------|---------------|---------------|------------|--------------------------|");
            for (List<String> elem : entry.getValue()) {
                String currency_value = String.format("\u20AC %s", formatter.format(Float.parseFloat(elem.get(3))).replaceAll("\\,00", ",-").replaceAll(" €", ""));
                String mileage_value = String.format("%s KM", formatter.format(Float.parseFloat(elem.get(4))).replaceAll("\\,00", "").replaceAll(" €", ""));
                System.out.println(String.format("| %7s | %10s | %13s | %13s | %10s | %24s |", elem.get(0), elem.get(1), elem.get(2), currency_value, mileage_value, elem.get(5)));
                System.out.println("|---------|------------|---------------|---------------|------------|--------------------------|");

            }
        }
    }

    public static void main (String [] args) {
        // Read listings
        List<List<String>> listings = readCSV("csv/listings.csv");
        List<String> header_listings = listings.remove(0);

        // Process seller types and average prices
        Set<String> seller_types = getSellerTypes(listings);
        Map<String, Float> average_selling_prices = getAverageSellingPrice(listings, seller_types);
        printSellerTypesAverages(average_selling_prices);

        // Percentual distribution of available cars by Make
        Set<String> makes = getMakes(listings);
        Map<String, Float> percentual_distribution_cars = getPercentualDistributionOfAvailableCars(listings, makes);
        printPercentualDistributionOfCarsByMake(percentual_distribution_cars);

        // Read contacts
        List<List<String>> contacts = readCSV("csv/contacts.csv");
        List<String> header_contacts = contacts.remove(0);

        // Average Price of Most Contacted Listings
        Map<String, Integer> frequencies = getMostContactedListings(listings, contacts);
        float average_price_most_contacted_listings = getAveragePriceOfMostContactedListings(listings, frequencies, 30f);
        printAveragePriceOfMostContactedListings(average_price_most_contacted_listings);

        // Top 5 Most Contacted Listings per Month
        Map<String, List<List<String>>> top_listings_per_month = getTopMostContactedListingsPerMonth(listings, contacts, 5);
        printTopMostContactedListingsPerMonth(top_listings_per_month);
    }
}