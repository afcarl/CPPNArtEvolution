package edu.southwestern.parameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import edu.southwestern.evolution.crossover.network.TWEANNCrossover;
import edu.southwestern.evolution.genotypes.TWEANNGenotype;
import edu.southwestern.evolution.selectiveBreeding.SelectiveBreedingEA;
import edu.southwestern.experiment.evolution.LimitedSinglePopulationGenerationalEAExperiment;
import edu.southwestern.networks.ActivationFunctions;
import edu.southwestern.tasks.interactive.breedesizer.Keyboard;
import edu.southwestern.util.random.GaussianGenerator;
import edu.southwestern.util.sound.SoundUtilExamples;
import edu.southwestern.util.stats.Average;
import edu.southwestern.util.stats.Max;

/**
 * Used for processing and containing command line parameters.
 *
 * @author Jacob Schrum
 */
public class Parameters {

	public static Parameters parameters;
	public ParameterCollection<Integer> integerOptions;
	public ParameterCollection<Long> longOptions;
	public ParameterCollection<Boolean> booleanOptions;
	public ParameterCollection<Double> doubleOptions;
	public ParameterCollection<String> stringOptions;
	// Class can be any type, hence <T> details are inappropriate
	@SuppressWarnings("rawtypes") 
	public ParameterCollection<Class> classOptions;

	/**
	 * Initialize the static Parameters instance using command line parameters.
	 *
	 * @param args
	 *            String array from command line
	 */
	public static void initializeParameterCollections(String[] args) {
		String logFile = getLogFilename(args);
		parameters = new Parameters(args);

		if (logFile != null) {
			System.out.println("File exists? " + logFile);
			File f = new File(logFile);
			if (f.getParentFile().exists() && f.exists()) {
				System.out.println("Load parameters: " + logFile);
				initializeParameterCollections(logFile);
				// Commandline can overwrite save file
				parameters.parseArgs(args, true);
			}
		}
		String base = parameters.stringParameter("base");
		if (base != null && !base.equals("")) {
			File baseDir = new File(base);
			if (!baseDir.exists() || !baseDir.isDirectory()) {
				System.out.println("Made directory: " + base);
				baseDir.mkdir();
			}
		}
		CommonConstants.load();
	}

	/**
	 * Load file name filled with parameters and use contents to fille all
	 * parameter collections in the standard static Parameters instance
	 *
	 * @param parameterFile
	 *            file to load from
	 */
	public static void initializeParameterCollections(String parameterFile) {
		if (parameters == null) {
			parameters = new Parameters(new String[0]);
		}
		System.out.println("Loading parameters from " + parameterFile);
		parameters.loadParameters(parameterFile);
		CommonConstants.load();
	}

	/**
	 * Load file name filled with parameters and use contents to fill all
	 * parameter collections.
	 *
	 * @param filename
	 *            File name to load parameters from
	 */
	public void loadParameters(String filename) {
		try (Scanner file = new Scanner(new File(filename))) {
			ArrayList<String> args = new ArrayList<String>();
			while (file.hasNextLine()) {
				String line = file.nextLine();
				args.add(line);
			}
			String[] sArgs = new String[args.size()];
			parseArgs(args.toArray(sArgs), false);
		} catch (FileNotFoundException ex) {
			System.out.println("Could not read parameter file");
			System.exit(1);
		}
	}

	/**
	 * Initialize parameter collections of each needed type
	 *
	 * @param args
	 *            Original String array of command line arguments
	 */
	@SuppressWarnings("rawtypes")
	public Parameters(String[] args) {
		booleanOptions = new ParameterCollection<Boolean>();
		classOptions = new ParameterCollection<Class>();
		doubleOptions = new ParameterCollection<Double>();
		integerOptions = new ParameterCollection<Integer>();
		longOptions = new ParameterCollection<Long>();
		stringOptions = new ParameterCollection<String>();

		fillDefaults();
		parseArgs(args, true);
	}

	/**
	 * Save parameters to the path and filename specified by the "base",
	 * "saveTo", "log", and "runNumber" parameters
	 */
	public void saveParameters() {
		String path = stringParameter("base") + "/" + stringParameter("saveTo") + integerParameter("runNumber");
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdir();

		}
		String name = stringOptions.get("log") + integerParameter("runNumber") + "_parameters.txt";
		this.saveParameters(path + "/" + name);
	}

	/**
	 * Save parameters to specified filename
	 *
	 * @param filename
	 *            Name of file to save parameters in
	 */
	public void saveParameters(String filename) {
		// PrintStream will be cleaned up as part of the try
		try (PrintStream stream = new PrintStream(new FileOutputStream(filename))) {
			integerOptions.writeLabels(stream);
			longOptions.writeLabels(stream);
			booleanOptions.writeLabels(stream);
			doubleOptions.writeLabels(stream);
			stringOptions.writeLabels(stream);
			classOptions.writeLabels(stream);
		} catch (FileNotFoundException ex) {
			System.out.println("Could not save parameters");
			System.exit(1);
		}
	}

	/**
	 * Define all parameter labels, default values, and help text
	 */
	public final void fillDefaults() {
		// Integer parameters
		integerOptions.add("imageSize", 200, "Size of image for Picbreeder");
		integerOptions.add("imageHeight", 2000, "height of CPPN image (overrides height of image being matched if overrideImageSize is true)");
		integerOptions.add("imageWidth", 2000, "width of CPPN image (overrides width of image being matched if overrideImageSize is true)");
		integerOptions.add("multinetworkComboReached", 0, "Tracks highest multinetwork combo reached so far to allow resuming after failure");
		integerOptions.add("numActiveGhosts", 4, "Number of moving ghosts in pacman");
		integerOptions.add("rawInputWindowSize", 5, "Raw input window size");
		integerOptions.add("startingModes", 1, "Modes that a network starts with");
		integerOptions.add("pacManLevelTimeLimit", 1000000, "Time steps per level until pacman dies");
		integerOptions.add("edibleTaskTimeLimit", 2000, "Time steps per level for edible ghost only subtask");
		integerOptions.add("maxModes", 1000, "Mode mutation cannot add more than this many modes");
		integerOptions.add("numModesToPrefer", -1, "If non-negative, then a fitness function rewards even usage of this many modes");
		integerOptions.add("bdArchiveSize", 0, "Maximum allowable size of archive for BD");
		integerOptions.add("initialPopulationSeed", -1, "Random seed used to determine the initial population");
		integerOptions.add("randomSeed", -1, "Random seed used to control algorithmic randomness (not domain randomness)");
		integerOptions.add("ftype", ActivationFunctions.FTYPE_TANH, "Integer designation of default activation function for networks");
		integerOptions.add("maxGens", 500, "Maximum generations allowed for a LimitedGenerationalEAExperiment");
		integerOptions.add("mu", 20, "Size of parent population in mu +/, lambda scheme");
		integerOptions.add("lambda", 50, "Size of child population in mu +/, lambda scheme");
		integerOptions.add("trials", 1, "Number of trials each individual is evaluated");
		integerOptions.add("teams", 1, "Number of teams each individual is evaluated in for coevolution");
		integerOptions.add("steps", 10000, "Maximum time steps in RL-Glue episode");
		integerOptions.add("syllabusSize", 10, "Number of examples in BD syllabus");
		integerOptions.add("numBreve2DMonsters", 4, "Number of evolving breve 2D monsters");
		integerOptions.add("breve2DTimeLimit", 1000, "Number of time steps allowed in breve 2D domains");
		integerOptions.add("breve2DAgentHealth", 50, "Hitpoints of agents in breve 2D domains");
		integerOptions.add("lastSavedGeneration", 0, "Last generation where genotypes were saved");
		integerOptions.add("runNumber", 0, "Number to designate this run of an experiment");
		integerOptions.add("threads", 4, "Number of threads if evaluating in parallel");
		integerOptions.add("multitaskModes", 1, "Number of multitask modes (1 if not multitask at all)");
		integerOptions.add("pacmanLives", 1, "Lives that a pacman agent starts with");
		integerOptions.add("hiddenMLPNeurons", 10, "Number of hidden neurons for MLPs");
		integerOptions.add("numMonsterRays", 5, "Number of ray trace sensors on each monster");
		integerOptions.add("litterSize", 10, "Number of offspring from a single source to evaluate for culling methods");
		integerOptions.add("cleanFrequency", -1, "How frequently the archetype needs to be cleaned out");
		integerOptions.add("pacmanMaxLevel", 4, "Pacman level after which simulation ends");
		integerOptions.add("justMaze", -1, "If 0 - 3, then Pac-Man only plays a specific maze over and over");
		integerOptions.add("initialMaze", 0, "Pacman maze to start on");
		integerOptions.add("ghostsForBonus", 17, "Ghosts that need to be eaten per level to get bonus evals");
		integerOptions.add("smallStepSimDepth", 30, "Forward simulation depth for variable direction sensors");
		integerOptions.add("escapeNodeDepth", 0, "How deep to forward simulate with escape nodes");
		integerOptions.add("layersToView", 1, "How many Pareto layers to view in multinetwork experiment");
		integerOptions.add("utNumOpponents", 1, "Number of opponents to evolve against in UT2004");
		integerOptions.add("utEvalMinutes", 5, "Number of minutes an evaluation in UT2004 lasts");
		integerOptions.add("utNativeBotSkill", 3, "Skill (1-7) of native UT2004 bots to evaluate against");
		integerOptions.add("utEvolvingBotSkill", 7, "Skill (1-7) of evolving UT2004 bot (affects accuracy)");
		integerOptions.add("evaluationBudget", 0, "Number of extra evals that UCB1 has to work with");
		integerOptions.add("maxTrials", Integer.MAX_VALUE, "Max trials allowed by individual when using UCB1 or increasing trials");
		integerOptions.add("fsLinksPerOut", 1, "Initial links per output with feature selective nets");
		integerOptions.add("numCoevolutionSubpops", 0, "When evolving a selector, number of populations of subcontrollers to choose from");
		integerOptions.add("recentPastMemLength", -1, "Length of queue of past pacman states");
		integerOptions.add("trialIncreaseFrequency", 1, "If increasing trials, do so every time this many generations pass");
		integerOptions.add("keeperCampLimit", 0, "Number of camps allowed to persist across generations");
		integerOptions.add("numberPredsSensedByPreds", -1, "the number of closest predators that will be sensed by other predators (all by default, see boolean)");
		integerOptions.add("numberPreySensedByPreds", -1, "the number of closest prey that will be sensed by predators (all by default, see boolean)");
		integerOptions.add("numberPredsSensedByPrey", -1, "the number of closest predators that will be sensed by prey (all by default, see boolean)");
		integerOptions.add("numberPreySensedByPrey", -1, "the number of closest prey that will be sensed by other prey (all by default, see boolean)");
		integerOptions.add("torusXDimensions", 100, "The dimension of the X-Axis, or the width, for the grid world");
		integerOptions.add("torusYDimensions", 100, "The dimension of the Y-Axis, or the height, for the grid world");
		integerOptions.add("torusTimeLimit", 1000, "Time limit in torus worlds");
		integerOptions.add("torusPredators", 3, "Number of torus predators");
		integerOptions.add("torusPreys", 2, "Number of torus preys");
		integerOptions.add("proxGhostsToSense", 4, "Number of ghosts sorted by proximity to sense in pacman");
		integerOptions.add("freezeMeltAlternateFrequency", 25, "Generations between freezing/melting pref/policy neurons");
		integerOptions.add("genOfLastTUGGoalIncrease", 0, "Generation when last TUG goal increase occurred");
		integerOptions.add("tugAdvancementTimeLimit", Integer.MAX_VALUE, "How many gens new goals can remain unachieved before RWAs are reset");
		integerOptions.add("disabledMode", -1, "If non-negative, then the designated mode can never be used");
		integerOptions.add("endTUGGeneration", Integer.MAX_VALUE, "Generation at which TUG will stop being used");
		integerOptions.add("startTUGGeneration", -1, "Generation at which TUG will start being used");
		integerOptions.add("deltaCodingFrequency", 20, "How often to generate a delta coded population");
		integerOptions.add("clipLength", Keyboard.NOTE_LENGTH_DEFAULT, "Length of clip played in Breedesizer");
		integerOptions.add("maxClipLength", 100000, "Maximum length of clip played in Breedesizer");
		integerOptions.add("minimaxSearchDepth", 2, "Tree-Search Depth of the Minimax BoardGamePlayer");
		integerOptions.add("remixImageWindow", 10, "Size of window being remixed by CPPN in Picture Remixer");
		integerOptions.add("minRemixImageWindow", 2, "Minimum size of window being remixed by CPPN in Picture Remixer");
		integerOptions.add("maxRemixImageWindow", 100, "Maximum size of window being remixed by CPPN in Picture Remixer");
		integerOptions.add("minAnimationLength", 10, "Minimum length of animation in AnimationBreeder");
		integerOptions.add("minAnimationLength", 10, "Minimum length of animation in AnimationBreeder");
		integerOptions.add("defaultAnimationLength", 50, "Default length of animation in AnimationBreeder");
		integerOptions.add("maxAnimationLength", 250, "Maximum length of animation in AnimationBreeder");
		integerOptions.add("minPause", 10, "Minimum pause length between each iteraton of animation in AnimationBreeder");
		integerOptions.add("defaultPause", 50, "Default pause length between each iteraton of animation in AnimationBreeder");
		integerOptions.add("maxPause", 500, "Maximum pause length between each iteraton of animation in AnimationBreeder");
		integerOptions.add("defaultFramePause", 50, "Default pause length between frames in AnimationBreeder");
		integerOptions.add("steadyStateIndividualsPerGeneration", 400, "How many individuals count as a log generation for SteadyStateEAs");
		integerOptions.add("numShapeInnovationSamples", 3, "Number of angles to take 2D image of 3D shape from for shape innovation task");
		integerOptions.add("receptiveFieldSize", 3, "Size of input windows for convolutional structures");
		integerOptions.add("stride", 1, "Offset between receptive fields in convolutional structures");
		integerOptions.add("hybrIDSwitchGeneration", 100, "Generation when HybrID should switch from HyperNEAT to NEAT");
		integerOptions.add("microRTSInputSize", 1, "how many by how many tiles in microRTS should be smudged together into a single input");
		integerOptions.add("defaultPitch", 36, "Default pitch value for 3DObjectbreeder vertical rotation");
		integerOptions.add("defaultHeading", 36, "Default heading value for 3DObjectbreeder horizaontal rotation");
		integerOptions.add("microRTSInputSize", 1, "x by x cells in the game that are condensed into a single input");
		integerOptions.add("boardGameStaticOpponentRuns", 1, "Number of Board Game matches to play against the Static Opponent Fitness Function");
		integerOptions.add("hallOfFamePastGens", 10, "Number of Generations in the past to use for the Hall Of Fame");
		integerOptions.add("hallOfFameNumChamps", 10, "Number of Champions from the Hall Of Fame to play against");
		integerOptions.add("gvgaiLevel", 0, "GVGAI level to be played; must be 0 - 4");
		integerOptions.add("gvgaiReactField", 5, "Size of the square to be evaluated by the GVGAI Reactive Player; must be an odd number");
		integerOptions.add("boardGameOpeningRandomMoves", 0, "In board games, making the first few moves random makes evals noisy in a useful way");
		integerOptions.add("rlBatchSize", 20, "Number of state transitions to log before doing an RL experience replay batch update");
		integerOptions.add("marioLevelLength", 60, "Length of Mario level generated by CPPNs");
		// Long parameters
		longOptions.add("lastInnovation", 0l, "Highest innovation number used so far");
		longOptions.add("lastGenotypeId", 0l, "Highest genotype id used so far");
		// Boolean parameters
		booleanOptions.add("inputsUseID", false, "Input neurons start with the ID activation function");
		booleanOptions.add("boardGameWinPercentFitness", false, "Is percentage of games won a fitness function for board games?");
		booleanOptions.add("boardGameIncreasingRandomOpens", false, "Number of random moves at the start of each game increases as evolved agents improve");
		booleanOptions.add("substrateBiasLocationInputs", false, "HyperNEAT uses coordinates of substrates (not just in substrates) as CPPN inputs when evolving bias values");
		booleanOptions.add("substrateLocationInputs", false, "HyperNEAT uses coordinates of substrates (not just in substrates) as CPPN inputs for weight link values");
		booleanOptions.add("heterogeneousSubstrateActivations", false, "HyperNEAT phenotypes can have a mix of activation functions");
		booleanOptions.add("convolution", false, "HyperNEAT phenotypes allow convolutional structure");
		booleanOptions.add("convolutionDeltas", false, "HyperNEAT encoding of convolutional structure uses the delta offset approach of Verbancsics");
		booleanOptions.add("convolutionWeightSharing", false, "HyperNEAT convolutional networks repeat the same weights across all receptive fields from one substrate to another");
		booleanOptions.add("zeroPadding", false, "Whether the input border for convolutional structures is padded with zeros");
		booleanOptions.add("averageScoreHistory", false, "Surviving parent fitness averaged across generations");
		booleanOptions.add("inheritFitness", false, "Child fitness is partially inherited from parents");
		booleanOptions.add("penalizeSubstrateLinks", false, "Whether to use additional fitness punishing substrate links");
		booleanOptions.add("randomizeSeedWeights", false, "randomizes all the weights in a hyperNEAT-seeded task");
		booleanOptions.add("showWeights", false, "visualizes weights of all links in network. Compatible for HyperNEAT only currently");
		booleanOptions.add("senseHyperNEATGhostPath", false, "shows nearest path to ghost with stronger activation as pacman gets closer to the ghost");
		booleanOptions.add("sortOutputActivations", false, "shows activations as sorted from most activated to least activated");
		booleanOptions.add("showHighestActivatedOutput", false, "highlights most activated output neuron as green in visualizations");
		booleanOptions.add("pacmanFullScreenProcess", false, "full screen process layer");
		booleanOptions.add("pacmanBothThreatAndEdibleSubstrate", false, "whether or not to have 2 substrates for threat and edible ghosts");
		booleanOptions.add("pacmanFullScreenPowerInput", false, "full screen input for power pill substrate");
		booleanOptions.add("pacManFullScreenOutput", false, "Output substrate has a cell for every 5x5 block on pacman screen");
		booleanOptions.add("moMario", false, "Mario is multiobjective");
		booleanOptions.add("showCPPN", false, "shows evolved CPPN during post evals");
		booleanOptions.add("splitRawTetrisInputs", false, "splits holes and blocks into two separate input substrates");
		booleanOptions.add("tetrisAllowLine", true, "Determines whether or not Line pieces will shown up in Tetris");
		booleanOptions.add("tetrisAllowSquare", true, "Determines whether or not Square pieces will shown up in Tetris");
		booleanOptions.add("tetrisAllowTri", true, "Determines whether or not Tri pieces will shown up in Tetris");
		booleanOptions.add("tetrisAllowSShape", true, "Determines whether or not SShape pieces will shown up in Tetris");
		booleanOptions.add("tetrisAllowZShape", true, "Determines whether or not ZShape pieces will shown up in Tetris");
		booleanOptions.add("tetrisAllowLShape", true, "Determines whether or not LShape pieces will shown up in Tetris");
		booleanOptions.add("tetrisAllowJShape", true, "Determines whether or not JShape pieces will shown up in Tetris");
		booleanOptions.add("evolveHyperNEATBias", true, "adds an output to evolved cppn that outputs bias of node");
		booleanOptions.add("extraHNLinks", false, "adds connections between the input and output substrate layers of hyperNEAT substrates");
		booleanOptions.add("showMarioInputs", false, "Shows the Mario input frame to the user as the agent would see them");
		booleanOptions.add("senseHolesDifferently", false, "Makes inputs for a hole different than input of a blank space");
		booleanOptions.add("showVizDoomInputs", false, "Shows the VizDoom inputs to the user as the agent would see them");
		booleanOptions.add("scaleTrials", false, "Whether or not to scale the number of trials as the number of generations increases");
		booleanOptions.add("leo", false, "LEO: Link Expression Output, CPPN has 2 outputs, separates presence and weight of link");
		booleanOptions.add("monitorSubstrates", false, "Allows us to visualizen the features (raw inputs) of a HyperNEAT agent");
		booleanOptions.add("doomFullScreenInput", false, "Detemines if we should use all of the screen inputs or just the given row");
		booleanOptions.add("hyperNEAT", false, "Use the HyperNEAT version of the task (requires HyperNEATCPPNGenotype)");
		booleanOptions.add("saveAllChampions", false, "saves all champions of each generation");
		booleanOptions.add("watchLastBest", false, "shows best result from last generation");
		booleanOptions.add("watchLastBestOfTeams", false, "shows best result from each population from last generation (coevolution)");
		booleanOptions.add("logChildScores", false, "For Mu/Lambda approaches that generate separate parent/child populations, indicates whether to log child info");
		booleanOptions.add("overrideImageSize", false, "For image match task, draw CPPNs with different size than actual image size");
		booleanOptions.add("logTWEANNData", false, "Whether or not to log TWEANN data");
		booleanOptions.add("logMutationAndLineage", false, "Whether or not to log information about the mutations and lineage");
		booleanOptions.add("logPerformance", false, "Whether or not to log performance information in a performance log");
		booleanOptions.add("indivPredMinDistIndivPrey", false, "Turn on to encourage the individual predator to be as close to the individual prey as possible by the end of the game");
		booleanOptions.add("indivPredMinDist", false, "Turn on to encourage the individual predator to be as close to the prey as possible by the end of the game");
		booleanOptions.add("indivPreyMaxDistIndivPred", false, "Turn on to encourage the individual prey to be as far from the individual predator as possible by the end of the game");
		booleanOptions.add("indivPreyMaxDist", false, "Turn on to encourage the individual prey to be as far from the predators as possible by the end of the game");
		booleanOptions.add("predsSenseAllPreds", true, "When using proximity sensors, causes predators to sense all other predators (always true for non-proximity sensors)");
		booleanOptions.add("predsSenseAllPrey", true, "When using proximity sensors, causes predators to sense all other prey (always true for non-proximity sensors)");
		booleanOptions.add("preySenseAllPreds", true, "When using proximity sensors, causes prey to sense all other predators (always true for non-proximity sensors)");
		booleanOptions.add("preySenseAllPrey", true, "When using proximity sensors, causes prey to sense all other prey (always true for non-proximity sensors)");
		booleanOptions.add("torusInvertSensorInputs", false, "Causes agents' sensor inputs to be inverted in torusPredPreyTask");
		booleanOptions.add("torusSenseByProximity", true, "Causes agents' sensor inputs to be by proximity of the agent instead of simply each agent by indices");
		booleanOptions.add("processHV", false, "Turns on the hyperVolume processing in postProcess, off by default");
		booleanOptions.add("homogeneousTeamSelection", false, "Homogeneous Predators with encouraged distance minimization and maximizing prey caught for all populations at once");
		booleanOptions.add("homogeneousAggregateTeamSelection", false, "Homogeneous Predators with encouraged distance minimization and maximizing prey caught as a team");
		booleanOptions.add("homogeneousTeamAndAggregateTeamSelection", false, "Homogeneous Predators with encouraged distance minimization and maximizing prey caught as a team and for all populations at once");
		booleanOptions.add("allowMultipleFunctions", true, "Turning this one will allow you to change TWEANN to CPPN by allowing multiple activation functions");
		booleanOptions.add("includeSigmoidFunction", false, "Fuction for sigmoid. If true, add to the function set");
		booleanOptions.add("includeFullSigmoidFunction", true, "Fuction for sigmoid stretched to [-1,1]. If true, add to the function set");
		booleanOptions.add("includeTanhFunction", false, "Fuction for tanh. If true, add to the function set");
		booleanOptions.add("includeIdFunction", true, "Just the sum. If true, add to the function set");
		booleanOptions.add("includeFullApproxFunction", false, "Fuction for full quick sigmoid. If true, add to the function set");
		booleanOptions.add("includeApproxFunction", false, "Fuction for quick sigmoid. If true, add to the function set");
		booleanOptions.add("includeGaussFunction", false, "Fuction for gaussian. If true, add to the function set");
		booleanOptions.add("includeFullGaussFunction", true, "Fuction for full gaussian. If true, add to the function set");
		booleanOptions.add("includeSineFunction", true, "Fuction for sine. If true, add to the function set");
		booleanOptions.add("includeCosineFunction", true, "Fuction for cosine. If true, add to the function set");
		booleanOptions.add("includeSawtoothFunction", false, "Fuction for sawtooth. If true, add to the function set");
		booleanOptions.add("includeAbsValFunction", false, "Fuction for absolute value. If true, add to the function set");
		booleanOptions.add("includeFullLinearPiecewiseFunction", false, "Function for full linear picewise. If true, add to the function set");
		booleanOptions.add("includeHalfLinearPiecewiseFunction", false, "Function for half linear picewise. If true, add to the function set");
		booleanOptions.add("includeStretchedTanhFunction", false, "Function for stretched tanh function. Good for preventing saturation if values in -1 - 1 range. If true, add to the function set.");
		booleanOptions.add("includeReLUFunction", false, "Function for rectified linear unit function. If true, add to the function set");	
		booleanOptions.add("includeSoftplusFunction", false, "Function for softplus function. If true, add to the function set");
		booleanOptions.add("includeLeakyReLUFunction", false, "Function for leaky rectified linear unit function. If true, add to the function set");
		booleanOptions.add("includeFullSawtoothFunction", true, "Function for full sawtooth function. If true, add to the function set");
		booleanOptions.add("includeTriangleWaveFunction", false, "Function for triangle wave function. If true, add to the function set");
		booleanOptions.add("includeSquareWaveFunction", true, "Function for square wave function. If true, add to the function set");
		booleanOptions.add("includeSiLFunction", false, "Function for sigmoid weighted linear unit function. If true, add to the function set");
		booleanOptions.add("finalPassOnOutputActivation", false, "Empty all remaining activation from network output layer");
		booleanOptions.add("onlyModeMutationWhenModesSame", false, "Only allow mode mutation if whole population has same number of modes");
		booleanOptions.add("tugGoalsIncreaseWhenThrashing", false, "Slightly increase TUG goals when there is evidence of thrashing");
		booleanOptions.add("stopTUGGoalDropAfterAchievement", false, "Initially dropping TUG goals stop dropping after first achievement (must be initially set high)");
		booleanOptions.add("tugGoalDropPossible", true, "Option that gest disabled by stopTUGGoalDropAfterAchievement so behavior is consistent on save and resume");
		booleanOptions.add("checkEachAbsoluteDistanceGhostSort", false, "Sort ghost sensors by their shortest path distance rather than directional path distance");
		booleanOptions.add("constantTUGGoalIncrements", false, "Goals increase for set specific amounts in each objective");
		booleanOptions.add("setInitialTUGGoals", false, "Initial TUG goals for each objective are set by hand");
		booleanOptions.add("tugObjectiveModeLinkage", false, "In TUG, modes and objectives are linked so that deactivated objectives have their modes frozen");
		booleanOptions.add("tugObjectiveUsageLinkage", false, "In TUG, when modes and objectives are linked, linkage depends on mode usage");
		booleanOptions.add("scalePillsByGen", false, "Number of pills scales with generation");
		booleanOptions.add("evalReport", false, "Write file of details for each eval");
		booleanOptions.add("initCrossCombine", false, "Use combining crossover on starting population");
		booleanOptions.add("policyFreezeUnalterable", false, "If a network is unalterable after crossover, then just freeze the policy");
		booleanOptions.add("prefFreezeUnalterable", false, "If a network is unalterable after crossover, then just freeze the preferences");
		booleanOptions.add("alternatePreferenceAndPolicy", false, "Alternately freeze and melt preference and policy neurons of multimodal networks");
		booleanOptions.add("meltAfterCrossover", false, "Melt frozen genes after crossover");
		booleanOptions.add("initAddPreferenceNeurons", false, "Add preference neurons for each mode of initial (loaded) population");
		booleanOptions.add("highLevel", true, "Use high-level sensors in mediators");
		booleanOptions.add("dieOnImproperPowerPillEating", false, "Pacman dies if power pill is eaten when less than 4 threat ghosts are present");
		booleanOptions.add("logLock", false, "Don't mess with log files at all");
		booleanOptions.add("rawTimeScore", false, "Encourage pacman to maximize time");
		booleanOptions.add("simultaneousLairExit", false, "Ghosts all exit lair at same time");
		booleanOptions.add("endOnlyOnTimeLimit", false, "Only thing that ends a pacman level is time running out");
		booleanOptions.add("exitLairEdible", false, "Ghosts are edible when exiting lair");
		booleanOptions.add("timeToEatAllFitness", false, "Fitness based on time to eat all ghosts after power pill");
		booleanOptions.add("infiniteEdibleTime", false, "Ghosts remain edible until eaten");
		booleanOptions.add("avgGhostsPerPowerPill", false, "Ghost score used is the average eaten per power pill eaten");
		booleanOptions.add("otherDirSensors", false, "Check-Each mediators include sensors that tell the current dir about other dirs");
		booleanOptions.add("pacManLureFitness", false, "Pacman evolved using luring fitness");
		booleanOptions.add("personalScent", false, "Pacman senses own scent");
		booleanOptions.add("initMMD", false, "Perform MMD on whole pop at start of run");
		booleanOptions.add("previousPreferences", false, "Sense previous time step direction preferences");
		booleanOptions.add("viewFinalCamps", false, "Look at final training camps from 'final'");
		booleanOptions.add("communalDeathMemory", false, "Sense locations of past deaths (requires logging death locations)");
		booleanOptions.add("randomArgMaxTieBreak", true, "Whenever multiple options have same value in argmax, pick random choice");
		booleanOptions.add("reachabilityReportsBuffers", false, "Reachability sensors give a sense of how safe a location is rather than just saying safe or not safe");
		booleanOptions.add("stepByStep", false, "Time steps only advances when Enter is pressed");
		booleanOptions.add("logDeathLocations", false, "Write to file every location where a pacman death occurs");
		booleanOptions.add("pacManSensorCaching", true, "Allows multiple networks to use same sensors without recalculating");
		booleanOptions.add("ghostRegretFitness", false, "Include negative fitness for ghosts that pacman fails to eat");
		booleanOptions.add("plainGhostScore", false, "For ghost fitness, just use eaten ghosts instead of ghost score");
		booleanOptions.add("ignoreGhostScores", false, "No fitness from edible ghosts in Ms Pac-Man, even though there are present");
		booleanOptions.add("levelObjective", false, "Add level objective to Ms Pac-Man");
		booleanOptions.add("consistentLevelObjective", false, "Level objective for Ms Pac-Man based on statistical mode");
		booleanOptions.add("requireFitnessDifferenceForChange", false, "If the tournament selection between two individuals reveals no fitness difference, then don't mutate or crossover the victor");
		booleanOptions.add("teamLog", false, "Log the score of every team evaluated");
		booleanOptions.add("ensembleModeMutation", false, "Different modes from mode mutation create ensemble");
		booleanOptions.add("awardProperPowerPillEating", false, "Fitness for eating power pills when all ghosts are threats");
		booleanOptions.add("punishImproperPowerPillEating", false, "Fitness against eating power pills when some ghosts are not threats");
		booleanOptions.add("viewModePreference", false, "Watch the behavior of preference neurons");
		booleanOptions.add("maximizeModes", false, "Meta-fitness to maximize number of modes");
		booleanOptions.add("bestTeamScore", true, "Coevolution assigns subcomponent the score of the best team it is in instead of AVG");
		booleanOptions.add("specificGhostProximityOrder", true, "Ghost specific sensors organize ghosts by proximity");
		booleanOptions.add("individualLevelFitnesses", false, "One fitness function for each level");
		booleanOptions.add("externalPreferenceNeurons", false, "Preference neuron outputs explicitly modelled as pacman output (not hidden in TWEANN)");
		booleanOptions.add("eachComponentTracksScoreToo", false, "Each subcomponent uses game score as reward in addition to preferred fitness");
		booleanOptions.add("ghostMonitorsSensePills", false, "Individual ghost monitors have redundant pill senses");
		booleanOptions.add("specificGhostEdibleThreatSplit", false, "Separate edible/threat sensors for specific ghost sensors");
		booleanOptions.add("staticLookAhead", false, "Include static look ahead sensors (no actual simulation)");
		booleanOptions.add("trapped", true, "Sense if ghosts trapped in corridor with pacman");
		booleanOptions.add("eTimeVsGDis", false, "Sense edible time minus ghost distance");
		booleanOptions.add("incoming", true, "Sense if ghosts are incoming or not");
		booleanOptions.add("mazeTime", false, "Use Pacman maze time sensors");
		booleanOptions.add("veryClose", true, "Use Pacman very close sensors");
		booleanOptions.add("lairDis", false, "Use Pacman lair distance sensors");
		booleanOptions.add("ghostTimes", true, "Use Pacman ghost times sensors");
		booleanOptions.add("specialPowerPill", false, "Use Pacman special power pill sensors");
		booleanOptions.add("specific", false, "Use Pacman specific ghost sensors");
		booleanOptions.add("nearestDis", true, "Use Pacman nearest distance sensors");
		booleanOptions.add("farthestDis", true, "Use Pacman farthest distance sensors");
		booleanOptions.add("nearestDir", true, "Use Pacman nearest direction sensors");
		booleanOptions.add("cluster", true, "Use Pacman ghost cluster sensors");
		booleanOptions.add("simIncludesExtraInfo", false, "Forward simulation also tells how many pills/power pills/ghosts are eaten");
		booleanOptions.add("sim", true, "Use Pacman forward simulation sensors");
		booleanOptions.add("staticSim", false, "Forward simulation is static rather than actually simulating");
		booleanOptions.add("diff", true, "Use Pacman distance difference sensors");
		booleanOptions.add("prox", false, "Use Pacman proximity sensors");
		booleanOptions.add("absolute", false, "Use Pacman absolute location sensors");
		booleanOptions.add("punishDeadSpace", false, "Pac-Man punished for time spent in dead space, ie not eating pills");
		booleanOptions.add("luringTask", false, "Pac-Man rewarded for luring ghosts to power pills before eating pill");
		booleanOptions.add("endAfterGhostEatingChances", false, "Advance to next level once eating more ghosts is impossible");
		booleanOptions.add("monitorInputs", false, "Show panel tracking input values");
		booleanOptions.add("rewardFasterPillEating", false, "Pill eating fitness gives higher fitness to eating pills quickly");
		booleanOptions.add("rewardFasterGhostEating", false, "Ghost reward fitness gives higher fitness to eating ghosts quickly after power pills");
		booleanOptions.add("minimizeSpliceImpact", false, "New splices have very small connection weights, and don't remove pre-existing link");
		booleanOptions.add("penalizeLinks", false, "Number of links is negative fitness");
		booleanOptions.add("penalizeLinksPerMode", false, "Combined with penalizeLinks, only penalize links per mode");
		booleanOptions.add("ucb1Evaluation", false, "Use UCB1 to decide which individuals get extra evaluations");
		booleanOptions.add("subsumptionIncludesInputs", false, "Subsumption arbitrator network accesses original inputs as well");
		booleanOptions.add("weightedAverageModeAggregation", false, "Merge multiple modes via weighted average of preference neurons");
		booleanOptions.add("afterStates", false, "Pacman picks action by looking at after states");
		booleanOptions.add("computeDirectionalPaths", true, "For pacman, compute/load all directional paths at the start instead of on the fly");
		booleanOptions.add("loadDirectionalPaths", false, "For pacman, load pre-computed directional paths if they exist");
		booleanOptions.add("saveDirectionalPaths", false, "For pacman, save directional paths computed for game");
		booleanOptions.add("noPowerPills", false, "No power pills in pacman");
		booleanOptions.add("noPills", false, "No regular pills in pacman");
		booleanOptions.add("ignorePillScore", false, "PacMan does not have pill fitness");
		booleanOptions.add("logPacManEvals", false, "Log score from every pacman game");
		booleanOptions.add("weakenBeforeModeMutation", false, "Existing network mode preferences are weakened before new mode is added");
		booleanOptions.add("freezeBeforeModeMutation", false, "Existing network is frozen before new mode is added");
		booleanOptions.add("cullModeMutations", false, "Cull different weightings of mode mutation synapses");
		booleanOptions.add("onlyWatchPareto", true, "When using LoadAndWatchExperiment, only watch the Pareto front");
		booleanOptions.add("animateNetwork", false, "Networks animate their activations");
		booleanOptions.add("rawScorePacMan", false, "Pac-Man uses Game Score as only fitness");
		booleanOptions.add("clearTimeScore", false, "Pac-Man rewarded for clearing level fast (single level only)");
		booleanOptions.add("erasePWTrails", true, "Puddle World trails are erased after each eval");
		booleanOptions.add("alwaysProcessPacmanInputs", false, "Pac-man inputs are processed on every time step, even when using decision points");
		booleanOptions.add("eliminateImpossibleDirections", true, "Pac-man only chooses from available directions to move");
		booleanOptions.add("pacManGainsLives", false, "Whether or not Pac-Man can gain new lives");
		booleanOptions.add("polynomialWeightMutation", false, "Network weights mutated with polynomial mutation");
		booleanOptions.add("tugKeepsParetoFront", false, "TUG favors the Pareto front before switching off objectives");
		booleanOptions.add("pacmanLevelClearingFitness", false, "Fitness favors finishing levels quickly in Ms. Pac-Man");
		booleanOptions.add("antiMaxModeUsage", false, "Negative fitness for highest percent mode usage, to encourage multiple mode use");
		booleanOptions.add("softmaxSelection", false, "Discrete action selection accomplished using softmax");
		booleanOptions.add("probabilisticSelection", false, "Discrete action selection probabilistic without using softmax");
		booleanOptions.add("softmaxModeSelection", false, "Mode selection accomplished using softmax");
		booleanOptions.add("connectToInputs", false, "TWEANN links can lead into input nodes");
		booleanOptions.add("lengthDependentMutationRate", true, "When using real-valued strings, mutation rate is 1/length");
		booleanOptions.add("io", true, "Write output logs");
		booleanOptions.add("netio", true, "Write xml files of networks");
		booleanOptions.add("fs", false, "Use feature selective initial networks instead of fully connected networks");
		booleanOptions.add("mating", true, "Use crossover to mate parents and get offspring");
		booleanOptions.add("polynomialMutation", true, "Real parameters mutated according to polynomial mutation");
		booleanOptions.add("watch", false, "Show evaluations during evolution");
		booleanOptions.add("watchFitness", false, "Show min/max fitness scores");
		booleanOptions.add("printFitness", false, "Print all scores from each evaluation");
		booleanOptions.add("showNetworks", false, "Show current TWEANN during evolution");
		booleanOptions.add("showSubnetAnalysis", false, "Show extra info about subnets in cooperative coevolution");
		booleanOptions.add("absenceNegative", false, "Sense absence of input as -1 instead of 0");
		booleanOptions.add("parallelEvaluations", false, "Perform evaluations in parallel");
		booleanOptions.add("parallelSave", false, "Perform file saving in parallel");
		booleanOptions.add("cleanOldNetworks", false, "Delete old network xml files once new networks are saved");
		booleanOptions.add("deterministic", false, "Make evaluations deterministic, if supported");
		booleanOptions.add("deleteLeastUsed", false, "Delete least-used mode when doing mode deletion");
		booleanOptions.add("relativePacmanDirections", true, "Ms. Pac-Man senses and actions for directions are relative to current direction");
		booleanOptions.add("moVizDoom", false, "VizDoom is multiobjective");
		booleanOptions.add("moPuddleWorld", true, "Puddle World is multiobjective, and separates step score from puddle score");
		booleanOptions.add("tetrisLinesNotScore", false, "For Tetris track lines cleared instead of game score");
		booleanOptions.add("tetrisTimeSteps", false, "For Tetris multiobjective, separates time steps from lines cleared");
		booleanOptions.add("tetrisBlocksOnScreen", false, "For Tetris multiobjective, seperates number of blocks on screen from lines cleared");
		booleanOptions.add("tetrisAvgEmptySpaces", false, "For Tetris multiobjective, average number of empty spaces after piece placements");
		booleanOptions.add("mmpActivationId", false, "Lateral MMP links use id function as activation function");
		booleanOptions.add("exploreWeightsOfNewStructure", false, "Evaluate multiple weight possibilities immediately after structural mutation");
		booleanOptions.add("cullCrossovers", false, "Cull a litter of different crossover possibilities");
		booleanOptions.add("mutationChancePerMode", false, "Genotype has one chance at each structural mutation per mode");
		booleanOptions.add("escapeToPowerPills", false, "Power pills are considered escape nodes");
		booleanOptions.add("nicheRestrictionOnModeMutation", false, "Only allow mode mutation to higher modes if max-mode niche is doing well");
		booleanOptions.add("pacmanMultitaskSeed", false, "Seed genotype for multitask run is combo of two separately evolved networks");
		booleanOptions.add("evolveNetworkSelector", false, "The evolved controller simply selects between the actions of other controllers");
		booleanOptions.add("multitaskCombiningCrossover", true, "If combining crossover is used, then network mode is chosen using a multitask scheme");
		booleanOptions.add("pacmanFatalTimeLimit", true, "Pacman dies if level time limit expires");
		booleanOptions.add("seedCoevolutionPops", false, "Coevolution pops start from pre-evolved pops");
		booleanOptions.add("utJumps", true, "UT2004 agent can jump");
		booleanOptions.add("defaultMediator", true, "For certain pacman coevolution experiments, all subnets use the same default mediator");
		booleanOptions.add("eligibilityOnEarnedFitness", false, "For earned fitness, track eligibility scores");
		booleanOptions.add("minimalSubnetExecution", false, "Don't execute subnets whose results are not needed");
		booleanOptions.add("limitedRecurrentMemory", false, "Reset subnet recurrent memory at the end of consecutive usage");
		booleanOptions.add("recurrency", false, "Allow recurrent links");
		booleanOptions.add("trialsMatchGenerations", false, "Trials increase with generations");
		booleanOptions.add("allowRandomGhostReversals", true, "Random ghost reversals happen in pacman");
		booleanOptions.add("pacManTimeFitness", false, "Fitness based on survival and speedy level completion");
		booleanOptions.add("imprisonedWhileEdible", false, "Ghosts cannot exit the lair as long as any ghost is edible");
		booleanOptions.add("randomSelection", false, "Only objective is a random objective");
		booleanOptions.add("tugResetsToPreviousGoals", false, "On TUG goal increase, reset RWAs to previous goals");
		booleanOptions.add("checkEachFlushWalls", true, "Check each direction mediators flush network for wall directions");
		booleanOptions.add("livesObjective", false, "Objective for remaining lives after beating final pac-man level");
		booleanOptions.add("periodicDeltaCoding", false, "Every few generations create child population by delta coding");
		booleanOptions.add("recordPacman", false, "Record pacman game to save file");
		booleanOptions.add("replayPacman", false, "Replay pacman game from save file");
		booleanOptions.add("hierarchicalMultitask", false, "Each multitask mode can consist of multiple preference neuron modules");
		booleanOptions.add("trackCombiningCrossover", false, "Whether or not to track combining crossover information");
		booleanOptions.add("moPinball", false, "Subtracts the distance to the target from the Fitness; getting closer means a higher score overall");
		booleanOptions.add("mRTSResourceProportion", false, "whether there is a substrate with a single input for the resource count");
		booleanOptions.add("mRTSMobileUnits", false, "whether there is a substrate dedicated to (both players') movable units");
		booleanOptions.add("mRTSBuildings", false, "whether there is a substrate dedicated to (both players') NON-movable units");
		booleanOptions.add("mRTSMyMobileUnits", false, "whether there is a substrate dedicated to blue agent's movable units");
		booleanOptions.add("mRTSMyBuildings", false, "whether there is a substrate dedicated to blue agent's NON-movable units");
		booleanOptions.add("mRTSOpponentsMobileUnits", false, "whether there is a substrate dedicated to red agent's movable units");
		booleanOptions.add("mRTSOpponentsBuildings", false, "whether there is a substrate dedicated to red agent's NON-movable units");
		booleanOptions.add("mRTSMyAll", false, "whether there is a substrate dedicated to all of the blue agents' units");
		booleanOptions.add("mRTSOpponentsAll", false, "whether there is a substrate dedicated to all of the red agents' units");
		booleanOptions.add("mRTSAll", true, "whether there is a substrate with everything in it");
		booleanOptions.add("mRTSResources", false, "whether there is a substrate with all resources in it");
		booleanOptions.add("mRTSTerrain", false, "whether there is a substrate with just un-crossable terrain in it");
		booleanOptions.add("mRTSObjectivePath", false, "whether there is a substrate with enemy bases and a gradient leading to them");
		booleanOptions.add("mRTSAllSqrt3MobileUnits", false, "whether there is a substrate with mobile units scored using the simple sqrt 3 evaluation function scoring");
		booleanOptions.add("mRTSMyBuildingGradientMobileUnits", false, "whether there is a substrate with my mobile units scored using the gradient to the enemy buildings");
		booleanOptions.add("heuristicOverrideTerminalStates", false, "Overrides the Network's evaluation of a terminal BoardGameState");
		booleanOptions.add("microRTSGrowingEnemySet", false, "adds multiple enemies to a trial instead of cycling through them by generation");
		booleanOptions.add("hybrID", false, "Indicates whether HybrID is running or not");
		booleanOptions.add("offsetHybrID", false, "Determines whether to implement preset-switch version of HybrID or offset version of HybrID");
		booleanOptions.add("hallOfFame", false, "Creates a Hall Of Fame during Single Population Coevolution");
		booleanOptions.add("hallOfFameSingleRandomChamp", true, "Only selects a single Random Champion from the Hall Of Fame");
		booleanOptions.add("hallOfFameXRandomChamps", false, "Selects a specified number of Random Champs from the Hall Of Fame; uses the hallOfFameNumChamps Parameter");
		booleanOptions.add("hallOfFameYPastGens", false, "Selects Hall Of Fame Champions from a specified number of Generations in the past; uses the hallOfFamePastGens Parameter");
		booleanOptions.add("hallOfFamePareto", false, "Removes Hall Of Fame Champions that are Pareto Dominated");
		booleanOptions.add("boardGameSimpleFitness", true, "Enables the SimpleWinLoseDraw BoardGame Fitness Function to be used as a Selection Function");
		booleanOptions.add("boardGameCheckersFitness", false, "Enables the CheckersAdvanced BoardGame Fitness Function to be used as a Selection Function");
		booleanOptions.add("boardGameOthelloFitness", false, "Enables the OthelloPiece BoardGame Fitness Function to be used as a Selection Function");
		booleanOptions.add("alwaysAnimate", true, "Loads and plays all animations in AnimationBreeder and 3DObjectBreeder at once");
		booleanOptions.add("saveInteractiveSelections", false, "Automatically saves all currently selected buttons when user moves to next generation");
		//booleanOptions.add("gvgaiSave", false, "If true, save the actions from the GVGAI game that was just played.");
		booleanOptions.add("gvgaiVictory", true, "Use the GVGAI Victory as a Selection Function");
		booleanOptions.add("gvgaiScore", false, "Use the GVGAI Score as a Selection Function");
		booleanOptions.add("gvgaiTimestep", false, "Use the GVGAI Timestep as a Selection Function");
		booleanOptions.add("simplifiedInteractiveInterface", true, "Determines how many buttons to show on the interactive evolution interfaces");
		booleanOptions.add("allowCubeDisplacement", true, "Allows displacement of individual voxels in 3D objects and animations");
		booleanOptions.add("rlBackprop", false, "Whether to do backprop learning updates during reinforcement learning");
		booleanOptions.add("rlEpsilonGreedy", false, "Whether to use an epsilon greedy policy when using reinforcement learning");
		// Double parameters
		doubleOptions.add("rlEpsilon", 0.1, "Frequency of completely random actions during Reinforcement Learning");
		doubleOptions.add("rlGamma", 0.99, "Discount factor used for Reinforcement Learning");
		doubleOptions.add("inheritProportion", 0.4, "Portion of a parent's fitness that contributes to child fitness (with inheritFitness, as in LEEA)");
		doubleOptions.add("linkExpressionThreshold", 0.2, "Threshold for hyperNEAT output to result in an expressed link");
		doubleOptions.add("tugGoalIncrement0", 0.0, "Set amount to increase goal 0 by when using TUG");
		doubleOptions.add("tugGoalIncrement1", 0.0, "Set amount to increase goal 1 by when using TUG");
		doubleOptions.add("tugGoalIncrement2", 0.0, "Set amount to increase goal 2 by when using TUG");
		doubleOptions.add("tugGoalIncrement3", 0.0, "Set amount to increase goal 3 by when using TUG");
		doubleOptions.add("tugGoalIncrement4", 0.0, "Set amount to increase goal 4 by when using TUG");
		doubleOptions.add("initialTUGGoal0", 0.0, "If TUG goals are set by hand, set objective 0 to this value");
		doubleOptions.add("initialTUGGoal1", 0.0, "If TUG goals are set by hand, set objective 1 to this value");
		doubleOptions.add("initialTUGGoal2", 0.0, "If TUG goals are set by hand, set objective 2 to this value");
		doubleOptions.add("initialTUGGoal3", 0.0, "If TUG goals are set by hand, set objective 3 to this value");
		doubleOptions.add("remainingTUGGoalRatio", 1.0,"What portion of TUG goal remains when objective is active (positive objectives only!)");
		doubleOptions.add("increasingTUGGoalRatio", 1.1,"If goals are increased on thrashing, then the increase results in this much remaining ratio (> 1)");
		doubleOptions.add("preferenceNeuronFatigueUnit", 0.0, "Amount of fatigue from preference neuron use");
		doubleOptions.add("preferenceNeuronDecay", 0.0,"Portion of remaining preference neuron fatigue each time step");
		doubleOptions.add("preEatenPillPercentage", 0.0,"Portion of pills that are eaten before the start of pacman eval");
		doubleOptions.add("powerPillPunishmentRate", 0.0,"Percent of time that pacman dies for failing to eat all ghosts");
		doubleOptions.add("scentDecay", 0.99, "Portion of scent remaining after each time step");
		doubleOptions.add("easyCampThreshold", 0.5, "Percent victories in camp that render it too easy");
		doubleOptions.add("hardCampThreshold", 0.25, "Percent victories in camp below which it must be saved");
		doubleOptions.add("percentDeathVsPPCamps", 0.5, "Percent of death camps (rest are PP camps");
		doubleOptions.add("campPercentOfTrials", 1.0, "What percentage trials should be based on camps");
		doubleOptions.add("percentDeathCampsToSave", 0.1, "What percentage of pre-death states to save for camps");
		doubleOptions.add("percentPowerPillCampsToSave", 0.025,"What percentage of pre-power pill states to save for camps");
		doubleOptions.add("aggressiveGhostConsistency", 0.9, "How often aggressive ghosts pursue pacman");
		doubleOptions.add("eligibilityLambda", 0.9, "Time decay on eligibility of rewards");
		doubleOptions.add("distanceForNewMode", -1.0,"If not -1, then behavioral distance between last two modes must be at least this much for mode mutation to occur");
		doubleOptions.add("usageForNewMode", 10.0,"The smaller this is (down to 1) the more restricted mode mutation is");
		doubleOptions.add("intReplaceRate", 0.3, "Rate for integer replacement mutation");
		doubleOptions.add("ghostGamma", 1.0, "Discount rate for ghost fitness in old pacman");
		doubleOptions.add("pillGamma", 1.0, "Discount rate for pill fitness in old pacman");
		doubleOptions.add("weakenPortion", 0.5, "How much the preference weakening operation weakens weights");
		doubleOptions.add("weightBound", 50.0, "The bound for network weights used by SBX and polynomial mutation");
		doubleOptions.add("softmaxTemperature", 0.25, "Temperature parameter for softmax selection");
		doubleOptions.add("tugAlpha", 0.3,"Step size for moving recency-weighted averages towards averages when using TUG");
		doubleOptions.add("tugEta", 0.3, "Step size for increasing goals when using TUG");
		doubleOptions.add("tugMomentum", 0.0, "Encourages TUG goals to maintain high rates of increase");
		doubleOptions.add("blueprintParentToChildRate", 0.9, "Mutation that swaps a pointer from a network to one of its children");
		doubleOptions.add("blueprintRandomRate", 0.5, "Mutation that swaps a pointer from a network to another random network in the appropriate subpopulation");
		doubleOptions.add("freezePolicyRate", 0.0, "Mutation rate for melting all then freezing policy neurons");
		doubleOptions.add("freezePreferenceRate", 0.0, "Mutation rate for melting all then freezing preference neurons");
		doubleOptions.add("freezeAlternateRate", 0.0, "Mutation rate for melting all then freezing policy or preference neurons (alternating)");
		doubleOptions.add("fullMMRate", 0.0, "Mutation rate for mode mutation that connects to all inputs");
		doubleOptions.add("deleteLinkRate", 0.0, "Mutation rate for deleting network links");
		doubleOptions.add("redirectLinkRate", 0.0, "Mutation rate for redirecting network links");
		doubleOptions.add("deleteModeRate", 0.0, "Mutation rate for deleting network modes");
		doubleOptions.add("mmpRate", 0.0, "Mutation rate for adding a new network mode (MM(P) for previous)");
		doubleOptions.add("mmrRate", 0.0, "Mutation rate for adding a new network mode (MM(R) for random)");
		doubleOptions.add("mmdRate", 0.0, "Mutation rate for adding a new network mode (MM(D) for duplication)");
		doubleOptions.add("netPerturbRate", 0.8, "Mutation rate for network weight perturbation");
		doubleOptions.add("perLinkMutateRate", 0.05, "Per link chance of weight perturbation");
		doubleOptions.add("netLinkRate", 0.4, "Mutation rate for creation of new network synapses");
		doubleOptions.add("netChangeActivationRate", 0.3, "Mutation rate for changing a neuron's activation function");
		doubleOptions.add("netSpliceRate", 0.2, "Mutation rate for splicing of new network nodes");
		doubleOptions.add("realMutateRate", 0.3, "Mutation rate for modifying indexes in real-valued string");
		doubleOptions.add("crossoverRate", 0.5, "Rate of crossover if mating is used");
		doubleOptions.add("mlpMutationRate", 0.1, "Rate of mutation for MLPs");
		doubleOptions.add("backpropLearningRate", 0.1, "Rate backprop learning for neural networks");
		doubleOptions.add("crossExcessRate", 0.0, "Portion of TWEANN crossovers that include excess/disjoint genes");
		doubleOptions.add("explorePreference", 0.5, "High for more exploration vs. low for more exploitation when using UCB1");
		doubleOptions.add("minimaxRandomRate", 0.0, "Chance of a Minimax Player choosing a Random legal Move instead of the best Move");
		doubleOptions.add("minimaxSecondBestRate", 0.0, "Chance of a Minimax Player choosing second best legal move instead of the best");
		doubleOptions.add("pictureInnovationSaveThreshold", 0.2, "Only saves pictures whose bin score surpasses this threshold");
		// String parameters
		stringOptions.add("archetype", "", "Network that receives all mutations so as to keep other networks properly aligned");
		stringOptions.add("seedArchetype1", "", "Archetype for seed sub-population 1");
		stringOptions.add("base", "", "Base directory for all simulations within one experiment");
		stringOptions.add("saveTo", "", "Prefix for subdirectory where output from one run will be saved");
		stringOptions.add("loadFrom", "", "Where ReplayEA loads networks from");
		stringOptions.add("log", "log", "Name of prefix for log files of experiment data");
		stringOptions.add("lastSavedDirectory", "", "Name of last directory where networks were saved");
		stringOptions.add("fixedMultitaskPolicy", "", "Path to xml file with multitask network, whose outputs control agent based on evolved preference selectors");
		stringOptions.add("fixedPreferenceNetwork", "", "Path to xml file with preference network, used on top of evolved multitask networks");
		stringOptions.add("seedGenotype", "", "Path to xml file with seed genotype for population");
		stringOptions.add("combiningCrossoverMapping", "", "File with HashMap from innovations in single mode nets to corresponding duplicate in multitask nets");
		stringOptions.add("branchRoot", "", "Evolve from some other run as starting point, based off of this parameter file");
		stringOptions.add("replayNetwork", "", "Network displayed while replaying pacman eval");
		stringOptions.add("pinballConfig", "pinball_simple_single.cfg", "Configuration file for the PinballTask");
		stringOptions.add("map", "8x8/basesWorkers8x8.xml", "filepath from maps folder to desired map file for MicroRTSTask");
		stringOptions.add("remixWAVFile", SoundUtilExamples.PORTAL2_WAV, "Input WAV file to be remixed in Remixbreeder");
		stringOptions.add("remixMIDIFile", SoundUtilExamples.FUR_ELISE_MID, "Input MIDI file to be played with CPPN in Breedesizer");
		stringOptions.add("gvgaiGame", "zelda", "GVGAI Game to be played");
		// Class options
		classOptions.add("weightPerturber", GaussianGenerator.class, "Random generator used to perturb mutated weights");
		classOptions.add("tugPerformanceStat", Average.class, "The stat used by TUG to calculate the performance of the population");
		classOptions.add("tugGoalTargetStat", Max.class, "The stat used by TUG to determine what value objective goals should work towards reaching");
		classOptions.add("ensembleArbitrator", null, "How to arbitrate between agents when using an ensemble");
		classOptions.add("crossover", TWEANNCrossover.class, "Crossover operator to use if mating is used");
		classOptions.add("nicheDefinition", null, "Method for getting the niche of an individual for local competition");
		classOptions.add("experiment", LimitedSinglePopulationGenerationalEAExperiment.class, "A subclass of Experiment to execute");
		classOptions.add("ea", SelectiveBreedingEA.class, "A subclass for the evolutionary algorithm to run");
		classOptions.add("task", null, "A subclass defining the task to solve");
		classOptions.add("genotype", TWEANNGenotype.class, "A subclass defining the genotype to evolve with");
	}

	/**
	 * Get boolean parameter with given label
	 *
	 * @param label
	 *            Parameter label
	 * @return corresponding boolean parameter label
	 */
	public boolean booleanParameter(String label) {
		return booleanOptions.get(label);
	}

	/**
	 * Get int parameter with given label
	 *
	 * @param label
	 *            Parameter label
	 * @return corresponding int parameter label
	 */
	public int integerParameter(String label) {
		return integerOptions.get(label);
	}

	/**
	 * Get long parameter with given label
	 *
	 * @param label
	 *            Parameter label
	 * @return corresponding long parameter label
	 */
	public long longParameter(String label) {
		return longOptions.get(label);
	}

	/**
	 * Get double parameter with given label
	 *
	 * @param label
	 *            Parameter label
	 * @return corresponding double parameter label
	 */
	public double doubleParameter(String label) {
		return doubleOptions.get(label);
	}

	/**
	 * Get String parameter with given label
	 *
	 * @param label
	 *            Parameter label
	 * @return corresponding String parameter label
	 */
	public String stringParameter(String label) {
		return stringOptions.get(label);
	}

	/**
	 * Get Class parameter with given label
	 *
	 * @param label
	 *            Parameter label
	 * @return corresponding Class parameter value
	 */
        // Class needs to be raw because any type can be returned
	@SuppressWarnings("rawtypes") 
	public Class classParameter(String label) {
		return classOptions.get(label);
	}

	/**
	 * Parse all command line parameters of each type
	 *
	 * @param args
	 *            The original String parameters
	 * @param terminateOnUnrecognized
	 *            Whether to exit program on invalid parameter
	 */
	private void parseArgs(String[] args, boolean terminateOnUnrecognized) {
		if (args.length > 0 && args[0].equals("help")) {
			System.out.println("Paremeter help:");
			usage(0);
		}
		StringTokenizer st;
		String entity = "";
		String value = "";
		for (String arg : args) {
			try {
				st = new StringTokenizer(arg, ":");
				entity = st.nextToken();
				if (st.hasMoreTokens()) {
					value = st.nextToken();
				} else {
					value = "";
				}
			} catch (Exception e) {
				System.out.println("Problem parsing \"" + arg + "\"");
				usage(1);
			}
			if (integerOptions.hasLabel(entity)) {
				integerOptions.change(entity, Integer.parseInt(value));
				System.out.println("Integer value \"" + entity + "\" set to \"" + value + "\"");
			} else if (longOptions.hasLabel(entity)) {
				longOptions.change(entity, Long.parseLong(value));
				System.out.println("Long value \"" + entity + "\" set to \"" + value + "\"");
			} else if (doubleOptions.hasLabel(entity)) {
				doubleOptions.change(entity, Double.parseDouble(value));
				System.out.println("Double value \"" + entity + "\" set to \"" + value + "\"");
			} else if (booleanOptions.hasLabel(entity)) {
				booleanOptions.change(entity, Boolean.parseBoolean(value));
				System.out.println("Boolean value \"" + entity + "\" set to \"" + value + "\"");
			} else if (stringOptions.hasLabel(entity)) {
				stringOptions.change(entity, value);
				System.out.println("String value \"" + entity + "\" set to \"" + value + "\"");
			} else if (classOptions.hasLabel(entity)) {
				try {
					classOptions.change(entity, Class.forName(value));
				} catch (ClassNotFoundException ex) {
					System.out.println(value + " is not a valid class");
					System.exit(1);
				}
				System.out.println("Class value \"" + entity + "\" set to \"" + value + "\"");
			} else {
				System.out.println("Did not recognize \"" + entity + "\" with value \"" + value + "\"");
				if (terminateOnUnrecognized) {
					usage(1);
				}
			}
		}
	}

	/**
	 * Based on the String arguments passed at the command line, extract
	 * the path and file name of the parameter log file (if it exists). 
	 * This makes it easy to check for an existing experiment run and
	 * resume it.
	 * @param args Same arguments passed to main from command line
	 * @return path and file name of potential parameter log file.
	 */
	public static String getLogFilename(String[] args) {
		String base = "";
		String saveTo = "";
		String log = "";
		String run = "";

		StringTokenizer st;
		String entity = "";
		String value = "";
		for (String arg : args) {
			try {
				st = new StringTokenizer(arg, ":");
				entity = st.nextToken();
				value = st.nextToken();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Problem parsing parameter tokens");
				System.exit(1);
			}
			if (entity.equals("saveTo")) {
				saveTo = value;
			} else if (entity.equals("log")) {
				log = value;
			} else if (entity.equals("runNumber")) {
				run = value;
			} else if (entity.equals("base")) {
				base = value;
			}
		}

		if (base.equals("") && saveTo.equals("")) {
			return null;
		}

		return base + "/" + saveTo + run + "/" + log + run + "_parameters.txt";
	}

        /**
         * Show the descriptive help message of each parameter
         * @param status The status that the program will exit with after
         *               showing the information.
         */
	public void usage(int status) {
		System.out.println("Usage:");
		System.out.println("Integer parameters:");
		integerOptions.showUsage();
		System.out.println("Long parameters:");
		longOptions.showUsage();
		System.out.println("Double parameters:");
		doubleOptions.showUsage();
		System.out.println("Boolean parameters:");
		booleanOptions.showUsage();
		System.out.println("String parameters:");
		stringOptions.showUsage();
		System.out.println("Class parameters:");
		classOptions.showUsage();
		System.exit(status);
	}

        /**
         * Set integer option value
         * @param label label for int parameter
         * @param value new value
         */
	public void setInteger(String label, int value) {
		this.integerOptions.change(label, value);
	}

        /**
         * Set long option value
         * @param label label for long parameter
         * @param value new value
         */
	public void setLong(String label, long value) {
		this.longOptions.change(label, value);
	}

        /**
         * Set double option value
         * @param label label for double parameter
         * @param value new value
         */
	public void setDouble(String label, double value) {
		this.doubleOptions.change(label, value);
	}

        /**
         * Set boolean option value
         * @param label label for boolean parameter
         * @param value new value
         */
	public void setBoolean(String label, boolean value) {
		this.booleanOptions.change(label, value);
	}

        /**
         * Set String option value
         * @param label label for String parameter
         * @param value new value
         */
	public void setString(String label, String value) {
		this.stringOptions.change(label, value);
	}

        /**
         * Set Class option value
         * @param label label for Class parameter
         * @param value new value
         */
	@SuppressWarnings("rawtypes")
	public void setClass(String label, Class value) {
		this.classOptions.change(label, value);
	}
}
