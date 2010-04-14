org.intrace description:

-- Functional Pieces --
1. Instrumenting Agent
2. CUI Remote Agent Config Tool
3. GUI Remote Agent Config Tool/Network Trace Viewer
4. Common Output Classes
5. Instrumentation Handlers


-- Packages (org.intrace.) --
agent               : Instrumenting Agent
agent.server        : IP Server for controlling the Agent, Output and Instrumentation Handlers

client.cui          : Command Line Agent Config Client
client.gui          : GUI Agent Config Client + Live Trace/Callers viewer
client.gui.helper   : Helper classes used by the GUI interface

output              : Common Output Classes
output.trace        : Trace Instrumentation Handler
output.callers      : Callers Analysis Instrumentation Handler

shared              : String constants used by both the Agent Server and the Client


      