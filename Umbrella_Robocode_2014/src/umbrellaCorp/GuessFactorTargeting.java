package umbrellaCorp;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/* Forma di statistica sul target:  
 * si cerca di capire in quale direzione bisogna sparare ogni volta, 
 * in modo da sparare in futuro nella direzione che si e' rivelata
 * corretta la maggior parte delle volte
 * 
 * Per conoscere il GF, bisogna sapere qual e' il maximum escape angle
 * e se il nemico si muove attorno a noi verso dx o verso sx
 * 
*/


/* Si usano le ONDE per trovare l'angolo e la direzione
 * a cui si dovrebbe sparare ogni volta
*/

class WaveBullet {
		
	// posizione da cui spariamo e angolo e potenza(velocita) del proiettile 
	public double startX, startY, startBearing, power;
	private long fireTime;	 	// quando spariamo
	public int direction;		//direzione del nemico rispetto a noi
	private int[] statistic;	
	public double enemyDistance; //distanza a cui si trova il nemico
	public double gFact;		 // GF, ovvero la direzione verso il nemico a firetime
	
	
	public WaveBullet(double x, double y, double bearing, double power, int direction, long time, int[] statistic) {
		this.startX = x;
		this.startY = y;
		this.startBearing = bearing;
		this.power = power;
		this.direction = direction;
		this.fireTime = time;
		this.statistic = statistic;
	}
	
	//max escape angle = angolo massimo teorico che il nemico potrebbe raggiungere rispetto a quando spariamo
	
	
	/*  
	 * Metodo che controlla se l'onda ha colpito il nemico.
	 * Se colpisce il nemico, cerchiamo di capire a quale GF e' 
	 * il nemico, cerchiamo quindi il corrispondente indice nel 
	 * vettore delle statistiche e quindi incrementiamo il valore 
	 * 
	 * TRUE se colpisce, FALSE altrimenti
	 * */
	
	public boolean checkHit(double enemyX, double enemyY, long currentTime) {
		// Da dove spariamo si origina l'onda.
		// Se la distanza tra dove spariamo ed il nemico e' <= alla distanza
		// percorsa dal proiettile, allora potremmo aver colpito il nemico
		
		if (Point2D.distance(startX, startY, enemyX, enemyY) <= (currentTime - fireTime) * Util.bulletVelocity(power)) 
		{
			//angolo tra noi quando spariamo ed il nemico
			double desiredDirection = Util.absoluteBearing(new Point2D.Double(startX,startY), new Point2D.Double(enemyX,enemyY));
			
			//offset tra angolo tra noi ed il nemico e quello del proiettile
			double angleOffset = Utils.normalRelativeAngle(desiredDirection - startBearing);
			
			//Calcoliamo il GF in relazione all'angolo di massima fuga 
			double guessFactor = Util.limit(-1, angleOffset / Util.maxEscapeAngle(Util.bulletVelocity(power)), 1) * direction;
			
			//calcoliamo l'indice con cui aggiornare la statistica per una maggiore precisione in futuro
			int index = (int) Math.round((statistic.length-1)/2 * (guessFactor+1));
			statistic[index]++;
			
			return true;	//l'onda ha colpito il nemico
		}
		
		return false; //l'onda non ha colpito il nemico
	}
	
}
	
/*
 * Guess fra -1 e 1 per capire dove sta andando il robot.
 * 	1 avanti fino al punto piu lontano che puo' raggiungere andando a max velocitA, 
 * -1 dietro,
 *  0 sta fermo 
 *  
 *  
 */
public class GuessFactorTargeting {

	// ----- lista delle onde da cui fare statistiche
	private List<WaveBullet> waves = new ArrayList<WaveBullet>();
	private MadRobot mr;
	
	// ----- statistiche GF sui proiettili sparati
	public static final int STATS_SIZE = 31;	//dispari cosi al centro e' 0
	private int[] stats = new int[STATS_SIZE];
	
	// ---- direzione nemico
	private int direction = 1;
	
	// ----- posizioni
	private Point2D.Double myLocation = new Point2D.Double();
	private Point2D.Double enemyLocation = new Point2D.Double();
	
	
	public GuessFactorTargeting(MadRobot mr) {
		this.mr = mr;
	}
	
	
	// ------ CREAZIONE E UPDATE ONDE -----
	
	public void onScannedRobot(ScannedRobotEvent e) {

		//angolo tra noi e il nemico
		double absBearing = mr.getHeadingRadians() + e.getBearingRadians();

	// ----- posizioni MadRobot e nemico -----
		
		myLocation.setLocation(mr.getX(), mr.getY());
		
		//posizione del nemico calcolata come posizione MadRobot+sin(absBearing)+enemy.distance
		enemyLocation.setLocation(Util.project(myLocation, absBearing, e.getDistance()));		
		double ex = enemyLocation.x;
		double ey = enemyLocation.y;
		
	//------------
		
		// processiamo le onde 
		for(int i=0; i<waves.size(); i++) {
			WaveBullet currentWave = (WaveBullet) waves.get(i);
			
			//se l'onda colpisce il nemico, la togliamo 
			if (currentWave.checkHit(ex, ey, mr.getTime())) {
				waves.remove(currentWave);
				i--;
			}
		}
		
		
		//potenza del proiettile calcolata con optimalPower
		double power = Math.min(3, Math.max(.1, mr.optimalPower(e.getDistance())));

		//direzione del nemico: se non si muove, prendiamo la direzione precedente
		if (e.getVelocity()!=0) {
			if(Math.sin(e.getHeadingRadians()-absBearing)*e.getVelocity()<0)
				direction=-1;
			else
				direction=1;
		}

		int[] currentStats = stats;
		
	//---- creazione dell'onda per lo scan corrente ----
		
		WaveBullet newWave = new WaveBullet(mr.getX(), mr.getY(), absBearing, power, direction, mr.getTime(), currentStats);
		newWave.enemyDistance = e.getDistance();
		
	//---- 	
		
		//---Cerchiamo l'indice migliore nella statistica
		int bestIndex=(STATS_SIZE-1)/2;	//all'inizio a meta' 
		
		for(int i=0; i<STATS_SIZE; i++)
			if(currentStats[bestIndex] < currentStats[i])
				bestIndex=i;
		
		//GF a partire dal bestindex
		double guessFactor = (double) (bestIndex-(STATS_SIZE-1)/2)/((STATS_SIZE-1)/2);
		newWave.gFact = guessFactor;
		
		/*calcola l'angolo con cui sparare in base a: 
			- direzione del nemico
		 	- GF appena calcolato 
		 	- max escape angle
		  gira quindi appositamente il cannone
		 */
		double angleOffset = direction*guessFactor*Util.maxEscapeAngle(Util.bulletVelocity(power));
		double gunAdjust = Utils.normalRelativeAngle(absBearing-mr.getGunHeadingRadians()+angleOffset);
		mr.setTurnGunRightRadians(gunAdjust);
		
//		if(mr.setFireBullet(power)!=null)
//			waves.add(newWave);
		
		
		//se il cannone e' freddo e se abbiamo effettivamente sparato, aggiungiamo l'onda 
		if(mr.getGunHeat()==0 && gunAdjust<Math.atan2(9, e.getDistance()) && mr.setFireBullet(power)!=null) {
			waves.add(newWave);
		}
				
	}

	public void onPaint(Graphics2D g) {
		WaveBullet wave = null;
		try {
			wave = waves.get(waves.size()-1);
		} catch (Exception e) {
			return;
		}
		g.setColor(Color.CYAN);
		int enemyX = (int) Util.project(new Point2D.Double(wave.startX,	wave.startY), wave.startBearing, wave.enemyDistance).x;
		int enemyY = (int) Util.project(new Point2D.Double(wave.startX,	wave.startY), wave.startBearing, wave.enemyDistance).y;

		int enemyMEAX = (int) Util.project(new Point2D.Double(wave.startX,
				wave.startY), wave.startBearing + Util.maxEscapeAngle(wave.power)
				* wave.direction, wave.enemyDistance).x;
		int enemyMEAY = (int) Util.project(new Point2D.Double(wave.startX,
				wave.startY), wave.startBearing + Util.maxEscapeAngle(wave.power)
				* wave.direction, wave.enemyDistance).y;

		g.drawLine(enemyX, enemyY, enemyMEAX, enemyMEAY);

		int bulletX = (int) Util.project(new Point2D.Double(wave.startX,
				wave.startY), wave.startBearing + Util.maxEscapeAngle(Util.bulletVelocity(wave.power))
				* wave.direction * wave.gFact, wave.enemyDistance).x;
		int bulletY = (int) Util.project(new Point2D.Double(wave.startX,
				wave.startY), wave.startBearing + Util.maxEscapeAngle(Util.bulletVelocity(wave.power))
				* wave.direction * wave.gFact, wave.enemyDistance).y;
		g.fillRect(bulletX, bulletY, 10, 10);
	}
		
}