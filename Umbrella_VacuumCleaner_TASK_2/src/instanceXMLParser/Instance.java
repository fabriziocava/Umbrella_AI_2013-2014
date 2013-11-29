package instanceXMLParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import core.VacuumEnvironment.LocationState;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */




/**
 *
 * @author davide
 */
public class Instance {


	//action order : up, down,left, right, suck

	/**
	 *
	 */
	protected Map<String,Double> actionCosts;

	protected int size;
	protected double energy;

	protected  Position agentPos;
	protected  Position basePos;

	protected LocationState[][] boardState;



	public Instance() {
		this.actionCosts = new HashMap<String, Double>();
		agentPos = new Position();
		basePos= new Position();


	}


	public void buildINstanceJDom(String path){
		//creating JDOM SAX parser
		SAXBuilder builder = new SAXBuilder();

		//reading XML document
		Document xml = null;
		try {
			xml = builder.build(new File(path));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//getting root element from XML document
		Element root = xml.getRootElement();
		List<Element> list = root.getChildren();
		for (Element element : list) {
			List<Element> list1;
			if(element.getName().equals("board")){
				list1=element.getChildren();
				for (Element element2 : list1) {
					if(element2.getName().equals("size")){//size of the space
						size= Integer.parseInt(element2.getText());
						//inizializzo matrice
						boardState= new LocationState[size][size];
						for (int j = 0; j < boardState.length; j++) {
							for (int k = 0; k < boardState.length; k++) {
								boardState[j][k]=LocationState.Clean;
							}
						}

					}else if (element2.getName().equals("tile_state")){//tile states
						int x,y;
						LocationState state = LocationState.Clean;
						String stateString;
						x=Integer.parseInt(element2.getAttribute("x").getValue());
						y=Integer.parseInt(element2.getAttribute("y").getValue());

						stateString = element2.getText();

						if(stateString.equals("obstacle")){
							state = LocationState.Obstacle;
						}else if(stateString.equals("dirty")){
							state = LocationState.Dirty;
						}

						boardState[x][y]=state;

					}
				}
			}else if(element.getName().equals("agent")){//agent
				List<Element> list3 = element.getChildren();
				for (Element element3 : list3) {
					if(element3.getName().equals("x")){
						agentPos.setX(Integer.parseInt(element3.getValue()));
					}else if(element3.getName().equals("y")){
						agentPos.setY(Integer.parseInt(element3.getValue()));
					}else if (element3.getName().equals("energy")) {
						energy=Double.parseDouble(element3.getValue());
					}
				}
			}else if(element.getName().equals("base")){//agent
				List<Element> list3 = element.getChildren();
				for (Element element3 : list3) {
					if(element3.getName().equals("x")){
						basePos.setX(Integer.parseInt(element3.getValue()));
					}else if(element3.getName().equals("y")){
						basePos.setY(Integer.parseInt(element3.getValue()));
					}
				}
			}else if(element.getName().equals("action_costs")){//agent
				List<Element> list3 = element.getChildren();
				for (Element element3 : list3) {
					if(element3.getName().equals("up") || element3.getName().equals("left") || 
							element3.getName().equals("down")|| element3.getName().equals("right")||
							element3.getName().equals("suck")){
						
						actionCosts.put(element3.getName(), Double.parseDouble(element3.getValue()));

					}
				}
			}
			
			
			
		}
	}


	@Override
	public String toString() {
		System.out.println("Vacuum_cleaner Instance:\n");
		System.out.println("Size: "+size);
		System.out.println("Agent Position: ("+agentPos.getX()+","+agentPos.getY()+")");
		System.out.println("Base Position: ("+basePos.getX()+","+basePos.getY()+")");
		System.out.println("energy: "+energy);
		System.out.println("Action Costs :");
		
		Set<String> keys=actionCosts.keySet();
		for (String string : keys) {
			System.out.println("\t"+string +": "+ actionCosts.get(string));
		}
		
		System.out.println("World :");
		for (int j = 0; j < boardState.length; j++) {
			System.out.print("\t");
			for (int k = 0; k < boardState.length; k++) {
				switch (boardState[j][k]) {
				case Clean:
					System.out.print("C ");
					break;

				case Obstacle:
					System.out.print("O ");
					break;

				case Dirty:
					System.out.print("D ");
					break;
				}
				
			}
			System.out.println();
		}

		
		return super.toString();
	}


	public Map<String, Double> getActionCosts() {
		return actionCosts;
	}


	public void setActionCosts(Map<String, Double> actionCosts) {
		this.actionCosts = actionCosts;
	}


	public int getSize() {
		return size;
	}


	public void setSize(int size) {
		this.size = size;
	}


	public double getEnergy() {
		return energy;
	}


	public void setEnergy(double energy) {
		this.energy = energy;
	}


	public Position getAgentPos() {
		return agentPos;
	}


	public void setAgentPos(Position agentPos) {
		this.agentPos = agentPos;
	}


	public Position getBasePos() {
		return basePos;
	}


	public void setBasePos(Position basePos) {
		this.basePos = basePos;
	}


	public LocationState[][] getBoardState() {
		return boardState;
	}


	public void setBoardState(LocationState[][] boardState) {
		this.boardState = boardState;
	}
	

	
	




}








