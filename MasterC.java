package filters;



import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;

public class MasterC {

	public static void main(String[] args) {
		try {
			Runtime runtime = Runtime.instance();
			ProfileImpl profileImp = new ProfileImpl(false);
			profileImp.setParameter(Profile.MAIN_HOST, "localhost");
			AgentContainer agentContainer = runtime.createAgentContainer(profileImp);
			AgentController agentController = agentContainer.createNewAgent("MasterAgent", "filters.MasterAgent", new Object[] {});
			agentController.start();
		} catch (ControllerException e) {
			
			e.printStackTrace();
		}

	}

}