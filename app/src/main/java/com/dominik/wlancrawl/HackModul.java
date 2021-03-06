package com.dominik.wlancrawl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by dominik on 28.02.17.
 */
public class HackModul implements Iterator<String>
{
    private String ssid;
    private int counter = 0;
    private List<String> generatedPasswords;
    private boolean random;
    private List<String> oldPasswords;

    public HackModul(boolean random, String ssid)
    {
        if (ssid == null)
        {
            throw new NullPointerException("ssid was null");
        }
        if (ssid.equals(""))
        {
            throw new IllegalArgumentException("ssid was empty");
        }
        this.ssid = ssid;

        this.random = random;

        generatedPasswords = new LinkedList<>();
        oldPasswords = new LinkedList<>();


        // generate list

        generatedPasswords.addAll(naivPasswords());
        generatedPasswords.addAll(addedNumPasswords());
        generatedPasswords.addAll(yearPasswords());
        generatedPasswords.addAll(dictionaryPasswords());

        // remove all passwords with length less then 8
        generatedPasswords = clear(generatedPasswords);
    }

    public boolean hasNext()
    {
        return counter < generatedPasswords.size();
    }

    public String next()
    {
        counter += 1;

        if (random)
        {
            Random rand = new Random();
            String currentPas = generatedPasswords.get(rand.nextInt(generatedPasswords.size()));
            while (oldPasswords.contains(currentPas))
            {
                currentPas = generatedPasswords.get(rand.nextInt(generatedPasswords.size()));
            }
            oldPasswords.add(currentPas);

            return currentPas;
        }
        else
        {
            return generatedPasswords.get(counter-1);           // current index is "counter", need to increment counter before return
        }

    }

    // HELPER METHODS that create password guesses

    private List<String> naivPasswords()
    {
        List<String> result = new LinkedList<>();
        result.add(ssid);                                   // "Maja Cafe"
        result.add(ssid.replace(" ", ""));                  // "MajaCafe"
        result.add(ssid.toLowerCase());                     // "maja cafe"
        result.add(ssid.replace(" ", "").toLowerCase());    // "majacafe"
        result.add(ssid.toUpperCase());                     // "MAJA CAFE"
        result.add(ssid.replace(" ", "").toUpperCase());    // "MAJACAFE"

        String[] parts = ssid.split(" ");
        if (parts.length > 1)
        {
            for (int i = 0; i < parts.length; i++)
            {
                result.add(parts[i]);                       // "Maja", "Cafe"
                result.add(parts[i].toLowerCase());         // "maja", "cafe"
                result.add(parts[i].toUpperCase());         // "MAJA", "CAFE"
            }
        }

        return result;
    }

    private List<String> dictionaryPasswords()
    {
        List<String> result = new LinkedList<>();
        result.add("guest");
        result.add("GUEST");
        result.add("freewifi");
        result.add("password");
        result.add("password1");
        result.add("123456");
        result.add("12345");
        result.add("1234567");
        result.add("12345678");
        result.add("0123456789");
        result.add("querty");

        return result;
    }

    private List<String> yearPasswords()
    {
        // todo: optimization would be to use password from naiv method and append year to all passwords


        List<String> result = new LinkedList<>();

        for (int i = 10; i < 18; i++)
        {
            result.add(ssid + Integer.toString(i));         // "Maja Cafe17"
            result.add(ssid + "20" + Integer.toString(i));  // "Maja Cafe2017"
        }

        return result;
    }

    private List<String> addedNumPasswords()
    {
        List<String> result = new LinkedList<>();

        for (int i = 0; i < 100; i++)
        {
            result.add(ssid + Integer.toString(i));
        }

        return result;
    }

    private List<String> clear(List<String> list)
    {
        List<String> result = new LinkedList<>();

        for (String s: list)
        {
            if (s.length() >= 8)
                result.add(s);
        }

        return result;
    }
}
