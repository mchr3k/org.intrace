package org.intrace.sandbox;

import java.util.List;
import java.util.Map;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector.Argument;

public class DebugTest
{
  private static final String field = "Foo";

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception
  {
    int ii = 10;
    threadID(Thread.currentThread().getId());
    System.out.println(ii);
    System.out.println(field);
  }

  private static void threadID(long id)
  {
    Thread dbg = new Thread(new Debugger(Thread.currentThread().getId(),
                                         Thread.currentThread().getName()));
    dbg.start();
    try
    {
      dbg.join();
    }
    catch (InterruptedException e)
    {
      // Throw away
      e.printStackTrace();
    }
  }

  public static class Debugger implements Runnable
  {
    private final String name;
    private final long id;

    public Debugger(long id, String name)
    {
      this.id = id;
      this.name = name;
    }

    @Override
    public void run()
    {
      VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
      List<AttachingConnector> connectors = vmm.attachingConnectors();
      for (AttachingConnector connector : connectors)
      {
        if (connector.name().contains("SocketAttach"))
        {
          try
          {
            Map<String, Argument> args = connector.defaultArguments();
            args.get("hostname").setValue("localhost");
            args.get("port").setValue("8000");
            VirtualMachine vm = connector.attach(args);
            useVM(vm);
            vm.dispose();
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
          break;
        }
      }
    }

    private void useVM(VirtualMachine vm)
    {
      List<ThreadReference> threads = vm.allThreads();
      for (ThreadReference thread : threads)
      {
        if (name.equals(thread.name()))
        {
          thread.suspend();
          try
          {
            int ii = 0;
            for (StackFrame frame : thread.frames())
            {
              if (matchingFrame(frame))
              {
                StackFrame callingFrame = thread.frame(ii + 1);
                useFrame(callingFrame);
              }
              ii++;
            }
            thread.resume();
          }
          catch (IncompatibleThreadStateException e)
          {
            e.printStackTrace();
          }
        }
      }
    }

    private boolean matchingFrame(StackFrame frame)
    {
      if ("threadID".equals(frame.location().method().name()) &&
          (frame.getArgumentValues().size() == 1) &&
          (frame.getArgumentValues().get(0).toString().equals(Long.toString(id))))
      {
        return true;
      }
      else
      {
        return false;
      }
    }

    private void useFrame(StackFrame frame)
    {
      try
      {
        System.out.println("Fields:");
        ReferenceType type = frame.location().declaringType();
        for (Field field : type.fields())
        {
          System.out.println(field.name() + " => " + type.getValue(field));
        }
        System.out.println("\nLocal Variables:");
        for (LocalVariable variable : frame.visibleVariables())
        {
          if (!variable.isArgument())
          {
            System.out.println(variable.name() + " => " + frame.getValue(variable));
          }
        }
        System.out.println("\nDone\n");
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

}
