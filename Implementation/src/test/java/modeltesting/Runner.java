package modeltesting;

import nz.ac.waikato.modeljunit.GraphListener;
import nz.ac.waikato.modeljunit.GreedyTester;
import nz.ac.waikato.modeljunit.StopOnFailureListener;
import nz.ac.waikato.modeljunit.Tester;
import nz.ac.waikato.modeljunit.coverage.ActionCoverage;
import nz.ac.waikato.modeljunit.coverage.StateCoverage;
import nz.ac.waikato.modeljunit.coverage.TransitionCoverage;
import nz.ac.waikato.modeljunit.coverage.TransitionPairCoverage;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

public class Runner {

    private static final Duration TEST_DURATION = Duration.ofMinutes(15);

    @Test
    public void main() {
        final Instant startTime = Instant.now();
        final Instant finishTime = startTime.plus(TEST_DURATION);

        System.setProperty("webdriver.chrome.driver", "chromedriver");

        final SystemModel model = new SystemModel();
        final Tester tester = new GreedyTester(model);
        tester.setRandom(new Random());
        final GraphListener gl = tester.buildGraph();
        tester.addListener(new StopOnFailureListener());
        tester.addCoverageMetric(new TransitionCoverage());
        tester.addCoverageMetric(new TransitionPairCoverage());
        tester.addCoverageMetric(new StateCoverage());
        tester.addCoverageMetric(new ActionCoverage());
        tester.addListener("verbose");
        while (Instant.now().isBefore(finishTime)) {
            tester.generate();
        }
        tester.printCoverage();
        model.quitWebDrivers();

        try {
            gl.printGraphDot("graph.dot");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
