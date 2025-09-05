package org.f1.modes;

import java.util.List;
import java.util.Set;

public class SeasonSimulator {

    private static final List<String> RACE_NAMES_2024 = List.of("Bahrain", "Saudi Arabia", "Australia", "Japan", "China", "Miami", "Imola", "Monaco", "Canada", "Spain", "Austria", "Britain", "Hungary", "Belgium", "Netherlands", "Italy", "Azerbaijan", "Singapore", "United States", "Mexico", "Brazil", "Las Vegas", "Qatar", "Abu Dhabi");
    private static final Set<String> SPRINTS_2024 = Set.of("China", "Miami", "Austria", "United States", "Brazil", "Qatar");

    public static void main(String[] args) {
        //List of Races

        //Set all driver and team costs to their base price

        //Create List of Agents

        //Agent determines how they choose which team to continue forwards with
        //Start with trying to figure out the optimal over time
        //Agents also store their current scorecard, their score, and their current costcap
        //Agents store their costchangechangemult

        //Loop through every race
        //Inside loop take all the agents and their current team and costcap and work out their best team
        //Replace team with new best team
        //Update costcap and update score with their actual score

        //At the end of the loop go through all drivers and use their actual score to work out their cost change and change the cost to reflect this


        //At the end loop through the agents outputting their score and how much their costcapchangemult was


    }
}
