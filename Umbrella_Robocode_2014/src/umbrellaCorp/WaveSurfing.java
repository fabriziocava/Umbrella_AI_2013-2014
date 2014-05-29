package umbrellaCorp;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;


/* SI PUO' MIGLIORARE:
 	- In choosing the wave to surf, adjust it to surf the one that will 
 	  hit you first, instead of the closest one. 
 	- Add evaluation for a stop position - you keep moving in the same 
 	  orbit direction, but hit the brakes (setMaxVelocity(0)) when you
 	  think that stop is much safer than moving. 
*/

class EnemyWave {
    Point2D.Double fireLocation;
    long fireTime;
    double bulletVelocity, directAngle, distanceTraveled;
    int direction;

    public EnemyWave() { 
    	
    }
}

/* Il Wave Surfing permette di individuare le posizioni 
 * possibili di un proiettile
 */
public class WaveSurfing {
	
	private MadRobot mr;
	public static int BINS = 47;	// firing angles suddivisi in bins discreti
	public static double surf_stats[] = new double[BINS];
	
	/*
	 * Bisogna tenere traccia:
	 * - della nostra posizione e di quella del nemico
	 * - del livello di energia del nemico (energy drop per sapere che e' stato sparato un proiettile)
	 * - lista di onde su cui fare statistiche
	 * - lista della nostra direzione, relativa ai turni giocati dal nemico
	 * - lista degli angoli (in valore assoluto) rispetto ai nemici  
	 */
	
	public Point2D.Double myLocation;
	public Point2D.Double enemyLocation;
	
	public ArrayList<EnemyWave> enemyWaves;
	public ArrayList<Integer> surfDirections;
	public ArrayList<Double> surfAbsBearings;
	
	public static double oppEnergy = 100.0;
	
	public static Rectangle2D.Double battlefieldRect;
	
	// distanza dal muro
	public static double WALL_STICK = 160;

	
	public WaveSurfing(MadRobot mr) {
		enemyWaves = new ArrayList<EnemyWave>();
		surfDirections = new ArrayList<Integer>();
		surfAbsBearings = new ArrayList<Double>();
		this.mr=mr;
	}
	
	public void init() {
		battlefieldRect = new java.awt.geom.Rectangle2D.Double(18, 18, MadRobot.battleFieldWidth-36, MadRobot.battleFieldHeight-36);
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		
		myLocation = new Point2D.Double(mr.getX(), mr.getY());
		
		// velocita'� perpendicolare al nemico, pari a 0 se si muove verso o si allontana da lui 
		double lateralVelocity = mr.getVelocity()*Math.sin(e.getBearingRadians());
		double absBearings = e.getBearingRadians() + mr.getHeadingRadians();
		
		//gira il radar a destra per il prox turno
		mr.setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearings-mr.getRadarHeadingRadians())*2);
		
		surfDirections.add(0, new Integer((lateralVelocity>=0)?1:-1));
		surfAbsBearings.add(0, new Double(absBearings+Math.PI));
		
		// E' stato sparato un proiettile con potenza pari all'energy drop
		double bulletPower = oppEnergy-e.getEnergy(); 
		
		//CREAZIONE ONDE quando viene sparato un proiettile 
			// surfDirections.size()>=2 perche' 2 turn prima di poter vedere energy drop
		if(bulletPower<=3 && bulletPower>=0.1 && surfDirections.size()>2) {
			
			EnemyWave ew = new EnemyWave();
			
			// AGGIORNO LE VARIABILI RELATIVE AL PROIETTILE
			ew.fireTime = mr.getTime()-1; // turno di gioco del nemico, precedente al mio
			ew.bulletVelocity = Util.bulletVelocity(bulletPower);
			ew.distanceTraveled = Util.bulletVelocity(bulletPower); // la distanza percorsa e' uguale alla velocita'� del proiettile?
			ew.direction = ((Integer) surfDirections.get(2)).intValue();
			ew.directAngle = ((Double) surfAbsBearings.get(2)).doubleValue();
			ew.fireLocation = (Point2D.Double) enemyLocation.clone(); // la sorgente del proiettile è la posizione del nemico
			
			// AGGIUNGO L'ONDA
			enemyWaves.add(ew);
		}
		//AGGIORNO L'ENERGIA e LA POSIZIONE DEL NEMICO una volta individuato, a partire dalla mia posizione
		oppEnergy = e.getEnergy();
		enemyLocation = Util.project(myLocation, absBearings, e.getDistance()); // usa sorgente,angolo,lunghezza e mi dà un punto
		
		updateWaves();
		doSurfing();		
	}
	
	/*
	 * Per ogni onda, aggiorno la distanza percorsa dalla sua sorgente (il nemico)
	 * Cancello le onde che non ci colpiscono e aggiungo 50 per avere un ulteriore
	 * spazio da tracciare con l'evento OnHitByBullet, altrimenti potremmo prendere 
	 * un proiettile la cui onda era stata cancellata.
	 */
	public void updateWaves() {
		for (int x=0; x<enemyWaves.size(); x++) {
			EnemyWave ew = (EnemyWave) enemyWaves.get(x);
			ew.distanceTraveled = (mr.getTime()-ew.fireTime)*ew.bulletVelocity;
			if (ew.distanceTraveled>myLocation.distance(ew.fireLocation)+50) {
				enemyWaves.remove(x);
				x--;
			}
		}
	}

	/*
	 * Per ogni onda, calcolo la distanza dalla mia posizione alla
	 * sorgente dell'onda (nemico). Se la distanza e' maggiore della
	 * velocita'� del proiettile e minore di 50000, l'onda da navigare 
	 * e' la corrente, che e' la piu' vicina. Infatti aggiorno closestDistance.
	 * In pratica, navigo l'onda finche' non attraversa il centro del mio robot.
	 * 
	 * Con questo metodo si trova quindi l'onda piu' vicina che non ha ancora
	 * attraversato il nostro robot, e la restituisce all'algoritmo del movimento.
	 * 
	 */
	public EnemyWave getClosestSurfableWave() {
		double closestDistance = 50000;
		EnemyWave surfWave = null;

		for (int x=0; x<enemyWaves.size(); x++) {
			EnemyWave ew = (EnemyWave) enemyWaves.get(x);
			double distance = myLocation.distance(ew.fireLocation) - ew.distanceTraveled;
			if (distance>ew.bulletVelocity && distance<closestDistance) {
				surfWave = ew;
				closestDistance = distance;
			}
		}
		return surfWave;
	}

	/*
	 * Data l'onda associata al proiettile e il punto dove siamo stati colpiti,
	 * calcola l'indice nell'array delle statistiche per quel fattore.
	 * 
	 * OffsetAngle = e' l'angolo corrente dal nostro robot alla sorgente dell'onda
	 * meno l'angolo originale dal nostro robot alla sorgente dell'onda (nell'istante
	 * in cui viene sparato il proiettile).
	 * Il GuessFactor e' proprio quest'angolo diviso l'angolo di massima fuga moltiplicato
	 * per la direzione (1 o -1, in modo da cambiare di segno se e' -1)   
	 */
	public static int getFactorIndex(EnemyWave ew, Point2D.Double targetLocation) {
		double offsetAngle = (Util.absoluteBearing(ew.fireLocation, targetLocation) - ew.directAngle);
		double factor = Utils.normalRelativeAngle(offsetAngle) / Util.maxEscapeAngle(ew.bulletVelocity) * ew.direction;
		
		/* conversione del fattore in un indice dell'array
		* A BINS/2 si ha GF=0 e si hanno BINS/2 celle a sinistra e altre BINS/2 celle a destra.
		* Per evitare di avere un numero tra - BINS/2 e BINS/2, si sposta l'intervallo
		* da 0 a BINS aggiungendo quindi BINS/2  
		*/
		return ((int) Util.limit(0, (factor*((BINS-1)/2))+((BINS-1)/2), BINS-1));
	}

	/*
	 * Data l'onda su cui viaggia il proiettile e il punto dove siamo stati colpiti
	 * aggiorniamo l'array della statistica per riflettere il pericolo in quell'area.
	 * Quando capiamo quale onda ci ha colpiti, possiamo usare la posizione del proiettile
	 *  che ci ha colpiti e aggiornare la statistica. Basta prendere l'indice dell'array 
	 *  restituito da getFactorIndex() per quel punto dell'onda e aggiornare con il Bin Smoothing
	 */
	public void logHit(EnemyWave ew, Point2D.Double targetLocation) {
		int index = getFactorIndex(ew, targetLocation);

		/* VCS - Quando si registra un colpo di proiettile, si aggiorna
		* di 1 il bin corrispondente a quell'angolatura da cui si spara.
		* Gli altri bin si incrementano di meno (Bin Smoothing)
		*/
		for (int x=0; x<BINS; x++) {
			surf_stats[x] += 1.0 / (Math.pow(index - x, 2) + 1);
		}
	}
	
	/*
	 * Quando un'onda ci colpisce, bisogna capire qual è.
	 * Per ogni onda, controlliamo se la distanza che ha percorso è a 
	 * 50 unità della nostra distanza corrente dalla sua sorgente.
	 * Controlliamo anche che la sua velocità sia la stessa della velocità 
	 * del proiettile che ci ha colpiti.  
	 * */

	public void onHitByBullet(HitByBulletEvent e) {
		
		/* Se l'array delle onde e' vuoto, non abbiamo identificato l'onda
		 * Se nn e' vuoto, prendiamo le coordinate della posizione del proiettile
		 * e fra tutte le onde cerchiamo quella che potrebbe averci colpito
		 * */
		if (!enemyWaves.isEmpty()) {
			Point2D.Double hitBulletLocation = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
			
			EnemyWave hitWave = null;
			
			for (int x=0; x<enemyWaves.size(); x++) {
				EnemyWave ew = (EnemyWave) enemyWaves.get(x);
				if (Math.abs(ew.distanceTraveled-myLocation.distance(ew.fireLocation))<50 && Math.abs(Util.bulletVelocity(e.getBullet().getPower())-ew.bulletVelocity)<0.001) {
					hitWave = ew;
					break;
				}
			}

			if (hitWave != null) {
			/* Una volta identificata l'onda che ci ha colpiti, chiamiamo logHit
			 * per aggiornare le statistiche.
			 * Posso quindi rimuovere quell'onda
			 **/ 
				logHit(hitWave, hitBulletLocation);
				enemyWaves.remove(enemyWaves.lastIndexOf(hitWave));
			}
		}
	}
	
	/* ALGORITMO PRECISE PREDICTION
	 * Data l'onda che stiamo navigando e la direzione verso cui orbitare
	 * (che e' quella che noi prevediamo), l'algortimo permette di predire il punto in cui
	 * saremo quando l'onda ci intercetta.
	 */

	public Point2D.Double predictPosition(EnemyWave surfWave, int direction) {
		Point2D.Double predictedPosition = (Point2D.Double) myLocation.clone();
		double predictedVelocity = mr.getVelocity();
		double predictedHeading = mr.getHeadingRadians();
		double maxTurning, moveAngle, moveDir;

		//num di turni in futuro
		int counter = 0; 
		boolean intercepted = false;

		/* Ad ogni turno predice l'angolo assoluto nel quale cerchiamo di muoverci.
		 * Orbitiamo, ovvero stiamo perpendicolari all'angolo della sorgente dell'onda.
		 * Una volta che abbiamo trovato l'angolo, lo passiamo al wallSmoothing
		 * */
		do {
			moveAngle = wallSmoothing(predictedPosition, Util.absoluteBearing(surfWave.fireLocation, predictedPosition) + (direction*(Math.PI/2)), direction)-predictedHeading;
			moveDir = 1;

			if (Math.cos(moveAngle)<0) {
				moveAngle += Math.PI;
				moveDir = -1;
			}

			moveAngle = Utils.normalRelativeAngle(moveAngle);

			//maximum turn rate : non si puo' girare piu' di questo in un turno
			maxTurning = Math.PI/720d*(40d-3d*Math.abs(predictedVelocity));
			predictedHeading = Utils.normalRelativeAngle(predictedHeading+Util.limit(-maxTurning, moveAngle, maxTurning));

			//se la velocità predetta e moveDir hanno diverso segno -> accelerazione
			predictedVelocity += (predictedVelocity*moveDir<0 ? 2*moveDir : moveDir);
			predictedVelocity = Util.limit(-8, predictedVelocity, 8);

			// nuova posizione predetta
			predictedPosition = Util.project(predictedPosition, predictedHeading, predictedVelocity);
			counter++;

			if (predictedPosition.distance(surfWave.fireLocation) < surfWave.distanceTraveled + (counter*surfWave.bulletVelocity) + surfWave.bulletVelocity) {
				intercepted = true;
			}
		} while (!intercepted && counter<500);

		return predictedPosition;
	}

	/* Ad ogni turno, controlliamo in quale direzione è più sicuro orbitare
	 * */
	public double checkDanger(EnemyWave surfWave, int direction) {
		int index = getFactorIndex(surfWave, predictPosition(surfWave, direction));
		return surf_stats[index];
	}

	/*
	 * Prediciamo la nostra posizione quando l'onda ci intercetta per ogni direzioe dell'orbita
	 * Una volta predetta la posizione, prendiamo il valore dall'array delle statistiche
	 * in base al GuessFactor di quella posizione.
	 * Scegliamo quindi la direzione piu' sicura in cui orbitare e applichiamo il wall smoothing ed il
	 * back-as-front per orbitare in quella direzione.
	 * 
	 * */
	public void doSurfing() {
		EnemyWave surfWave = getClosestSurfableWave();
		
		if (surfWave==null) {
			return;
		}
		
		double dangerLeft = checkDanger(surfWave, -1);
		double dangerRight = checkDanger(surfWave, 1);

		double goAngle = Util.absoluteBearing(surfWave.fireLocation, myLocation);

		if (dangerLeft < dangerRight) {
			goAngle = wallSmoothing(myLocation, goAngle - (Math.PI/2), -1);
		} else {
			goAngle = wallSmoothing(myLocation, goAngle + (Math.PI/2), 1);
		}

		Util.setBackAsFront(mr, goAngle);
	}

	/*
	 * ALGORITMO WALL SMOOTHING permette di andare vicino ai muri senza sbatterci contro
	 * Permette quindi di evitare le collisioni senza bisogno di cambiare direzione ogni volta,
	 * ma muovendosi lungo il muro.
	 */
	public double wallSmoothing(Point2D.Double botLocation, double startAngle, int orientation) {
		
		double angle = startAngle;
		double testX = botLocation.x + (Math.sin(angle)*WALL_STICK);
		double testY = botLocation.y + (Math.cos(angle)*WALL_STICK);
		double wallDistanceX = Math.min(botLocation.x-18, MadRobot.battleFieldWidth-botLocation.x-18);
		double wallDistanceY = Math.min(botLocation.y-18, MadRobot.battleFieldHeight-botLocation.y-18);
		double testDistanceX = Math.min(testX-18, MadRobot.battleFieldWidth-testX-18);
		double testDistanceY = Math.min(testY-18, MadRobot.battleFieldHeight-testY-18);
		
		double adjacent = 0;		
	    int g = 0; // because I'm paranoid about potential infinite loops
	 
	    while (!battlefieldRect.contains(testX, testY) && g++ < 25) {
	        if (testDistanceY < 0 && testDistanceY < testDistanceX) {
	            // wall smooth North or South wall
	            angle = ((int)((angle + (Math.PI/2)) / Math.PI)) * Math.PI;
	            adjacent = Math.abs(wallDistanceY);
	        } else if (testDistanceX < 0 && testDistanceX <= testDistanceY) {
	            // wall smooth East or West wall
	            angle = (((int)(angle / Math.PI)) * Math.PI) + (Math.PI/2);
	            adjacent = Math.abs(wallDistanceX);
	        }
	 
	        // use your own equivalent of (1 / POSITIVE_INFINITY) instead of 0.005
	        // if you want to stay closer to the wall ;)
	        angle += orientation*
	            (Math.abs(Math.acos(adjacent/WALL_STICK)) + 0.005);
	 
	        testX = botLocation.x + (Math.sin(angle)*WALL_STICK);
	        testY = botLocation.y + (Math.cos(angle)*WALL_STICK);
	        testDistanceX = Math.min(testX - 18, MadRobot.battleFieldWidth - testX - 18);
	        testDistanceY = Math.min(testY - 18, MadRobot.battleFieldHeight - testY - 18);
	    }
	 
	    return angle; // you may want to normalize this
		
	}
	

	public void onPaint(Graphics2D g) {
		g.setColor(Color.RED);
		for(EnemyWave wave : enemyWaves) {
			draw(g, wave);
		}
	}
	
	public void draw(Graphics2D g, EnemyWave ew) {
		final double x = ew.fireLocation.getX() - ew.distanceTraveled;
		final double y = ew.fireLocation.getY() - ew.distanceTraveled;
		final double diameter = ew.distanceTraveled*2;
		Shape circle = new Ellipse2D.Double(x, y, diameter, diameter);
		g.draw(circle);
	}

}
