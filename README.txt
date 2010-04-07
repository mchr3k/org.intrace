org.intrace description:

-- Packages --
agent        : Instrumenting Agent including IP Server
output       : Instrumentation output handlers
output.trace : Default (and currently only) handler

client/cui/AgentLoader : Command line runtime Agent loading tool
client/cui/TraceClient : Command line remote Agent config tool
client/gui/            : GUI remote Agent config tool/network trace viewer

-- Functional Pieces --
1. Instrumenting Agent
2. Runtime Agent Loader
3. CUI Remote Agent Config Tool
4. GUI Remote Agent Config Tool/Network Trace Viewer

Note to self - Git Commands:

  git init
  git remote add origin git@github.com:mchr3k/org.intrace.git
  git push origin master
      