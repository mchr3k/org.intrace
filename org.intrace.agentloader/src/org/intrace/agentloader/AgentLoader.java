package org.intrace.agentloader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * Command line agent loader.
 */
public class AgentLoader
{
  /**
   * Cmd line tool.
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception
  {
    BufferedReader readIn = new BufferedReader(new InputStreamReader(System.in));
    String inLine = "";
    while (!"quit".equals(inLine))
    {
      System.out.print("Enter command [attach/quit]: ");
      inLine = readIn.readLine();

      if ("attach".equals(inLine))
      {
        attachToVM(readIn);
      }
    }
  }

  private static void attachToVM(BufferedReader xiReadIn)
  {
    try
    {
      listProcIDs();
      System.out.print("Choose VM: ");
      String inLine = xiReadIn.readLine();
      if (!"".equals(inLine))
      {
        VirtualMachine vm = VirtualMachine.attach(inLine);
        System.out.println("Attached to: " + vm.id());

        while (!"done".equals(inLine))
        {
          System.out.print("Enter command [load/done]: ");
          inLine = xiReadIn.readLine();

          if ("load".equals(inLine))
          {
            loadAgent(xiReadIn, vm);
          }
        }
        vm.detach();
        System.out.println("Detatched from: " + vm.id());
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private static void loadAgent(BufferedReader xiReadIn, VirtualMachine vm) throws Exception
  {
    System.out.print("Enter path to agent jar (blank for windows default): ");
    String agentPath = xiReadIn.readLine();
    String defaultAgentPath = "../TestProject/lib/traceagent.jar";
    if ("".equals(agentPath))
    {
      agentPath = defaultAgentPath;
    }
    System.out.println("Agent Options");
    System.out.println("Leave blank for default: [regex-dcl.*");
    System.out.println("After loading you can only enabled/disable trace");
    System.out.print("Enter agent options: ");
    String agentOptions = xiReadIn.readLine();
    String defaultAgentOpt = "[regex-dcl.*";
    if ("".equals(agentOptions))
    {
      agentOptions = defaultAgentOpt;
    }
    vm.loadAgent(agentPath, agentOptions);
    System.out.println("Loaded agent into: " + vm.toString());
  }

  private static void listProcIDs()
  {
    System.out.println("============== Begin List ==============");
    List<VirtualMachineDescriptor> vmList = VirtualMachine.list();
    for (VirtualMachineDescriptor vm : vmList)
    {
      System.out.println(vm.displayName() + ":" + vm.id());
    }
    System.out.println("============== End List ==============");
  }
}
