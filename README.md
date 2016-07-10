# Instrumental complex for ontological engineering purpose
ICOP is a system that implements one of the areas of Data & Text Mining integrated technology, specifically – the analysis and processing of large volumes of unstructured data, such as linguistic corpus in Ukrainian and/or Russian languages, the extraction of subject knowledge and their subsequent representation in the form of system-ontological structure or DA ontology. ICOP is designed to implement multiple components of the unified information technology [3]:

 - Search the Internet and/or in other electronic collections of text
   documents (TD) that are relevant to the given DA, index and storage
   them into the database; Automatic natural language processing;
 - Extraction of knowledge from a variety of TD that relevant to given
   DA, their system- ontological structuring and formal-logical
   representation on one (or more) of the common ontology description
   languages (Knowledge Representation);
 - Creation, storage and use of large structures of ontological
   knowledge in the respective libraries;
 - System integration of ontological knowledge as a key component of the
   methodology of interdisciplinary research.
	
ICOP is composed of three sub-systems and represents the integration of various information resources, software and hardware processing tools (Firmware), and natural intelligence procedures, which interact with each. These sub-systems implemented a collection of algorithms for automated and iterative creation of the DA conceptual structures, their accumulation and/or system integration.
The Information resource subsystem includes the formation blocks of linguistic corpus, databases, language structures and libraries of conceptual structures. The first component represents a different text information sources that entering the processing system. The second component represents a different database processing of linguistic structures, some of which are formed (filled with data) in the processing of TD, and the other part is formed before the process of creation an DA ontology and, in fact, is a collection of various electronic dictionaries. The third component is a set of libraries of conceptual structures of different levels of representation (sets of terms and concepts to the ontological structure of a highly interdisciplinary knowledge).

The Firmware subsystem includes linguistic and conceptual structures processing units, and the management graphical user interface (GUI). The management GUI in collaboration with a knowledge engineer is responsible for overall control of the implementation of the related information technologies.
The Natural intelligence subsystem provides preparing and implementation of the pre-design phase, and throughout the process controls and verification of the results of the stages of design, decide the degree of completion (and, if necessary – repeating some of them) [3].

The current stage of development of the SS and software engineering industry is characterized by significant complexity of the process of their development. Under the SS is understood certain finite set of programs designed to achieve the objective(s). Functioning of the system is to use the included programs (software modules) in it and may be performed in two ways:

 - Single-threaded execution of processes (each moment of time the
   procedure is performed (operator) in one of the programs or running
   only one program (module) and all other programs (modules) to this
   time suspend their operation);
 - Multithreaded execution of processes (each moment of time the
   procedure is performed (operator) in each program (module) or several
   programs (modules) are executed in parallel).

Creation of the SS model should be guided by the following principles [9].
  1. Model of the SS should not be overly detailed (excessive complexity of the model can cause significant computational problems in its formal analysis).
  2. Model of the SS should not be overly simplified, it should reflect those aspects of the SS that are relevant to the property to check and maintain all the properties of the modelled system.
 
From the point of view of SS software engineering is considered as a set of descriptions provided in the form of mathematical models, descriptive formalisms and modelling techniques [4].
The structure of SS mathematical models of this type includes the following models [4]:
 – Data model;
 – Functionally distributed model.

The given work also includes UML diagrams (use case diagram, activity diagram, sequence diagram, class diagram) as a functionally distributed model of SS ICOP and some basic stuff about three-tier architecture of SS ICOP in the client-server environment.
