package edu.southwestern.experiment.post;

import edu.southwestern.evolution.genotypes.Genotype;
import edu.southwestern.experiment.Experiment;
import edu.southwestern.MMNEAT.MMNEAT;
import edu.southwestern.scores.Score;
import edu.southwestern.tasks.GroupTask;
import edu.southwestern.util.file.FileUtilities;
import edu.southwestern.util.graphics.DrawingPanel;

import java.util.ArrayList;
import wox.serial.Easy;

/**
 * Actually only works for coevolved Ms. Pac-Man experiments.
 * 
 * Load saved results from coevolution experiment and evaluate every possible
 * team combination to get their scores.
 *
 * @author Jacob Schrum
 */
public class BestCooperativeMsPacManTeamExperiment implements Experiment {

	private GroupTask task;
	@SuppressWarnings("rawtypes")
	private Genotype[] team;

	@SuppressWarnings("rawtypes")
	@Override
	public void init() {
		task = (GroupTask) MMNEAT.task;
		int numMembers = task.numberOfPopulations();
		team = new Genotype[numMembers];
		String teamDir = FileUtilities.getSaveDirectory() + "/bestTeam";
		for (int i = 0; i < numMembers; i++) {
			team[i] = (Genotype) Easy.load(teamDir + "/teamMember" + i + ".xml");
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void run() {
		DrawingPanel[] panels = GroupTask.drawNetworks(team);
		ArrayList<Score> result = task.evaluate(team);
		GroupTask.disposePanels(panels);
		for (Score s : result) {
			System.out.println(s);
		}
	}

	@Override
	public boolean shouldStop() {
		// Will never be called
		return true;
	}
}
