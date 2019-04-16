<body class='markdown-preview' data-use-github-style><p><img src="https://www.humanbrainproject.eu/image/company_logo?img_id=10795&amp;t=1480587142258" alt="Human Brain Project logo"></p>
<h1 id="medical-informatics-platform">Medical Informatics Platform</h1>
<h1 id="data-mining-algorithms-algorithm-developer-s-manual">Data Mining Algorithms - Algorithm developer&#39;s manual</h1>
<p>This is the repository containing all data mining algorithms for the <a href="https://www.humanbrainproject.eu/mip">Medical Informatics Platform</a> 
of <a href="https://www.humanbrainproject.eu/">Human Brain Project</a>, that are executed on <a href="https://www.exareme.org">Exareme</a>.</p>
<h2 id="types-of-algorithm-workflows-currently-supported">Types of algorithm workflows currently supported</h2>
<ul>
<li><code>local</code> execution of an SQL script on master node only.</li>
<li><code>local-global</code> execution of a <em>local</em> SQL script on <a href="http://madgik.github.io/exareme/architecture.html">worker nodes/containers</a>, 
which is followed by a <em>global</em> SQL script on global node.  </li>
<li><code>multiple local-global</code> execution of a fixed (as in fixed number) sequences of <code>local-global</code> workflows.<br>Each <code>local_global</code> is executed according to the order appeared in the algorithm&#39;s directory structure.  </li>
<li><p><code>iterative</code> execution of an iterative algorithm, which is expressed in four phases:  </p>
<ol>
<li><code>initialization</code> (actually a <code>multiple_local_global</code>)</li>
<li><code>step</code> (actually a <code>multiple_local_global</code>)</li>
<li><code>termination condition</code> (actually a <code>local</code>) and</li>
<li><code>finalization</code> (actually a <code>multiple_local_global</code>)</li>
</ol>
<p>Firstly, <code>init</code> phase is executed, which is followed by a <em>pair</em> of <code>step</code> and <code>termination_condition</code> phases. 
In each termination condition, the iterations module of execution engine reads the value of termination_condition related table and &quot;decides&quot; 
whether to continue the iterative execution. If so, a <code>step</code> phase is resubmitted, otherwise the <code>finalize</code> phase of the algorithm is
submitted.</p>
</li>
</ul>
<h3 id="expected-format-for-each-algorithm-workflow">Expected format for each algorithm workflow</h3>
<h4 id="algorithm-properties-file">Algorithm properties file</h4>
<p> For all algorithms, a <code>properties</code> file is required (namely <code>properties.json</code>). This <code>JSON</code> file contains the algorithm&#39;s:</p>
<ol>
<li>name</li>
<li>description (appears in web portal) </li>
<li>type, specifically one of:  <ul>
<li><code>local</code></li>
<li><code>local_global</code></li>
<li><code>multiple_local_global</code></li>
<li><code>iterative</code></li></ul>
<li>parameters<br>These parameters are required for the algorithm to run and are provided by the user. The algorithms' SQL files require these variables as input. 
The parameter has the following properties:<ul>
<li><code>name</code> (String) </li>
<li><code>desc</code> (String) Will be shown in the properties of the algorithm. </li>
<li><code>type</code> Defines the type of the parameter. It can take the following values: 
<ol>
<li><code>column</code> (Used for querying the columns of the database.)</li>
<li><code>filter</code> (Used to filter the results of the database.)</li>
<li><code>dataset</code> (If the property is of type dataset then it will be used to choose on which dataset to run the algorithm on.)</li>
<li><code>other</code> (For any other reason use this type.)</li>
</ol>
</li>
<li><code>value</code> (String) It is used as an example value. </li>
<li><code>valueNotBlank</code> (Boolean) Defines if the value can be blank.</li>
<li><code>valueMultiple</code> (Boolean) Defines if the parameter can have multiple values. </li>
<li><code>valueType</code> Defines the type of the value. It can take the following values: 
<ol>
<li><code>string</code></li>
<li><code>integer</code></li>
<li><code>real</code></li>
<li><code>json</code></li>
</ol>
</li>
</ul>
</ol>
</li>
</ol>
<p><strong>Example</strong>: See <a href="LINEAR_REGRESSION/properties.json">here</a> for the properties file of LINEAR_REGRESSION algorithm.</p>
<h4 id="expected-_directory_-structure-for-each-algorithm-workflow">Expected <em>directory</em> structure for each algorithm workflow</h4>
<p>For each algorithm workflow refer to the corresponding link for a hands-on example:</p>
<ol>
<li><code>local</code> =&gt; <a href="LIST_VARIABLES">LIST_VARIABLES algorithm</a></li>
<li><code>local_global</code> =&gt; <a href="VARIABLE_PROFILE">VARIABLE_PROFILE algorithm</a>  </li>
<li><code>multiple_local_global</code> =&gt; <a href="LINEAR_REGRESSION">LINEAR_REGRESSION algorithm</a>  </li>
<li><code>iterative</code> =&gt; <a href="SAMPLE_ITERATIVE">SAMPLE_ITERATIVE algorithm</a>  </li>
</ol>
<h2 id="general-directions-for-writing-algorithms">General directions for writing algorithms</h2>
<h3 id="input-of-algorithm-workflows">Input of algorithm workflows</h3>
<p>The input of algorithm workflows can be retrieved in the 1st <code>local.template.sql</code> by using the <code>input_local_tbl</code>
variable. (It must also be defined in <code>requirevars</code>.)</p>
<h3 id="sharing-context-among-different-sql-template-scripts">Sharing context among different SQL template scripts</h3>
<p><strong>defaultDB</strong><br>To share context (and thus data) among SQL template files, a database named <code>defaultDB</code> is provided.<br>For example, it can be used to create and insert values in a table at a <code>local.template.sql</code>, which can then be read from the <code>global.template.sql</code>.<br>To be able to use <code>defaultDB</code> in a <code>template.sql</code>, the script file is required to begin with:  </p>
<pre class="editor-colors lang-text"><div class="line"><span class="syntax--text syntax--plain"><span class="syntax--meta syntax--paragraph syntax--text"><span>-&nbsp;`requirevars&nbsp;&#39;defaultDB&#39;`&nbsp;(more&nbsp;variables&nbsp;can&nbsp;be&nbsp;_required_&nbsp;using&nbsp;this&nbsp;command,&nbsp;see&nbsp;[here](WP_LINEAR_REGRESSION/1/global.template.sql))</span></span></span></div><div class="line"><span class="syntax--text syntax--plain"><span class="syntax--meta syntax--paragraph syntax--text"><span>-&nbsp;`attach&nbsp;Database&nbsp;&#39;%{defaultDB}&#39;&nbsp;as&nbsp;defaultDB`&nbsp;&nbsp;</span></span></span></div></pre><p><strong>Output of previous phase</strong><br>An additional way of sharing context when in a <code>local_global</code> or <code>multiple_local_global</code> algorithm workflow is:  </p>
<ul>
<li>_[only for <code>multiple_local_global</code>]_ for <code>local.template.sql</code> files, the output from the previous <code>global.template.sql</code> execution can be read
by using the <code>input_local_tbl</code> variable.</li>
<li>for <code>global.template.sql</code> files, the output from the previous <code>local.template.sql</code> file can be read by using the <code>input_global_tbl</code> variable.</li>
</ul>
<p><strong>N.B.:</strong> It should be noted here, that <code>defaultDB</code> is shared over the network from local nodes to global and vice versa.</p>
<h3 id="every-template-file-must-have-output">Every template file must have output</h3>
<p>It is required by the runtime engine that every <code>*.template.sql</code> file must have some output.<br>If this isn&#39;t applicable in a script file, simply write <code>select &quot;ok&quot;;</code> at the end.</p>
<h3 id="algorithm-s-output-format">Algorithm&#39;s output format</h3>
<p>The final results (i.e. the algorithm&#39;s output) <strong>must</strong> be formatted using 
<a href="http://madgik.github.io/madis/row.html?highlight=jdict#functions.row.jpacks.jdict"><code>jdict</code></a> UDF of <a href="http://madgik.github.io/madis/">madIS</a>.<br>  This converts the results to a <code>JSON</code> format.</p>
<h3 id="specifics-pertaining-to-iterative-algorithms">Specifics pertaining to iterative algorithms</h3>
<h4 id="regarding-context-sharing-among-iteration-execution-phases">Regarding context sharing among iteration execution phases</h4>
<p>For sharing context among iteration execution phases, the <code>previous_phase_output_tbl</code> variable can be used. This follows the same convention
as the one used for sharing context between <code>local</code> and <code>global</code> scripts. In other words, output of the previous iterative execution phase
is &quot;forwarded&quot; as input to the next one (e.g. output of <code>step-1</code> is forwarded as input to <code>step-2</code> and output of <code>step-N</code> is forwarded 
as input to <code>finalize</code>).  </p>
<h4 id="regarding-the-properties-file">Regarding the properties file</h4>
<p>For all iterative algorithms (in the <code>parameters JSON array</code> of its properties file), the following properties must be defined:  </p>
<pre class="editor-colors lang-text"><div class="line"><span class="syntax--text syntax--plain"><span class="syntax--meta syntax--paragraph syntax--text"><span>-&nbsp;`iterations_max_number`&nbsp;&nbsp;&nbsp;</span></span></span></div><div class="line"><span class="syntax--text syntax--plain"><span>&nbsp;&nbsp;&nbsp;</span><span class="syntax--meta syntax--paragraph syntax--text"><span>The&nbsp;iterative&nbsp;algorithm&nbsp;will&nbsp;run&nbsp;at&nbsp;most&nbsp;`iterations_max_number`&nbsp;times.&nbsp;&nbsp;</span></span></span></div><div class="line"><span class="syntax--text syntax--plain"><span class="syntax--meta syntax--paragraph syntax--text"><span>-&nbsp;`iterations_condition_query_provided`&nbsp;&nbsp;&nbsp;&nbsp;</span></span></span></div><div class="line"><span class="syntax--text syntax--plain"><span>&nbsp;&nbsp;&nbsp;</span><span class="syntax--meta syntax--paragraph syntax--text"><span>Defines&nbsp;if&nbsp;a&nbsp;termination&nbsp;query&nbsp;is&nbsp;provided&nbsp;(under&nbsp;the&nbsp;`termination_condition`&nbsp;directory,&nbsp;in&nbsp;the&nbsp;corresponding&nbsp;file).&nbsp;</span></span></span></div><div class="line"><span class="syntax--text syntax--plain"><span class="syntax--meta syntax--paragraph syntax--text"><span>&nbsp;&nbsp;&nbsp;Otherwise&nbsp;`iterations_max_number`&nbsp;will&nbsp;be&nbsp;solely&nbsp;used&nbsp;as&nbsp;a&nbsp;termination&nbsp;condition&nbsp;criterion.&nbsp;&nbsp;</span></span></span></div><div class="line"><span class="syntax--text syntax--plain"><span class="syntax--meta syntax--paragraph syntax--text"><span>&nbsp;&nbsp;&nbsp;**Note&nbsp;1**:&nbsp;In&nbsp;the&nbsp;case&nbsp;which&nbsp;a&nbsp;termination&nbsp;condition&nbsp;query&nbsp;has&nbsp;been&nbsp;provided,&nbsp;the&nbsp;iterations&nbsp;module&nbsp;in&nbsp;Exareme&nbsp;takes&nbsp;into</span></span></span></div><div class="line"><span class="syntax--text syntax--plain"><span>&nbsp;&nbsp;&nbsp;&nbsp;</span><span class="syntax--meta syntax--paragraph syntax--text"><span>account&nbsp;its&nbsp;output&nbsp;along&nbsp;with&nbsp;the&nbsp;`iterations_number&nbsp;&lt;&nbsp;iterations_max_number`&nbsp;condition.&nbsp;&nbsp;</span></span></span></div><div class="line"><span class="syntax--text syntax--plain"><span class="syntax--meta syntax--bullet-point syntax--star syntax--text"><span>&nbsp;&nbsp;&nbsp;</span><span class="syntax--punctuation syntax--definition syntax--item syntax--text"><span>*</span></span><span>*Note&nbsp;2**:&nbsp;In&nbsp;the&nbsp;case&nbsp;which&nbsp;a&nbsp;termination&nbsp;condition&nbsp;query&nbsp;has&nbsp;**not**&nbsp;been&nbsp;provided,&nbsp;the&nbsp;`termination_condition.template.sql`</span></span></span></div><div class="line"><span class="syntax--text syntax--plain"><span>&nbsp;&nbsp;&nbsp;</span><span class="syntax--meta syntax--paragraph syntax--text"><span>must&nbsp;exist,&nbsp;and&nbsp;solely&nbsp;contain&nbsp;a&nbsp;`select&nbsp;&quot;ok&quot;;`&nbsp;query.</span></span></span></div></pre><h4 id="regarding-iterations-logic-requirements">Regarding iterations logic requirements</h4>
<p>The algorithm developer need not to worry about iterations control logic, such as setting up an iterations number counter, 
or writing a query for ensuring that <code>iterations_number &lt; iterations_max_number</code>. This is all handled by the iterations module
of Exareme.<br><strong>The only requirement imposed by the iterations module is the one mentioned below.</strong></p>
<h4 id="regarding-iterations_condition_query-">Regarding <code>iterations_condition_query</code></h4>
<p>If an iterative algorithm requires a termination condition that is not solely based on<br><code>iterations_number &lt; iterations_max_number</code> criterion, 
the algorithm developer needs to write a query that abides by the following rules:  </p>
<ul>
<li><em>updating</em> <code>iterationsDB.iterations_condition_check_result_tbl</code> table, and specifically</li>
<li><em>setting</em> <code>iterations_condition_check_result</code> column&#39;s value with the output of the termination condition query.  </li>
</ul>
<p>The template which must be followed is this:  </p>
<pre class="editor-colors lang-text"><div class="line"><span class="syntax--text syntax--plain"><span class="syntax--meta syntax--paragraph syntax--text"><span>update&nbsp;iterationsDB.iterations_condition_check_result_tbl&nbsp;set&nbsp;iterations_condition_check_result&nbsp;=&nbsp;(</span></span></span></div><div class="line"><span class="syntax--text syntax--plain"><span>&nbsp;&nbsp;</span><span class="syntax--meta syntax--paragraph syntax--text"><span>select&nbsp;termination_condition_query...&nbsp;</span></span></span></div><div class="line"><span class="syntax--text syntax--plain"><span class="syntax--meta syntax--paragraph syntax--text"><span>);</span></span></span></div></pre><p><strong>N.B.:</strong> <code>iterationsDB</code> does <strong>not</strong> need to be defined in the <code>requirevars</code> section. Again, this is handled by the runtime engine&#39;s iterations module.<br>An example of a termination condition query is presented below:  </p>
<pre class="editor-colors lang-text"><div class="line"><span class="syntax--text syntax--plain"><span class="syntax--meta syntax--paragraph syntax--text"><span>update&nbsp;iterationsDB.iterations_condition_check_result_tbl&nbsp;set&nbsp;iterations_condition_check_result&nbsp;=&nbsp;(</span></span></span></div><div class="line"><span class="syntax--text syntax--plain"><span>&nbsp;&nbsp;</span><span class="syntax--meta syntax--paragraph syntax--text"><span>select&nbsp;sum_tbl.sum_val&nbsp;&lt;&nbsp;5</span></span></span></div><div class="line"><span class="syntax--text syntax--plain"><span>&nbsp;&nbsp;&nbsp;&nbsp;</span><span class="syntax--meta syntax--paragraph syntax--text"><span>from&nbsp;defaultDB.sum_tbl</span></span></span></div><div class="line"><span class="syntax--text syntax--plain"><span class="syntax--meta syntax--paragraph syntax--text"><span>);</span></span></span></div></pre><p>In this example, the iterative algorithm calculates a sum (saved at <code>defaultDB.sub_tbl</code> table) and the termination condition reads: </p>
<blockquote>
<p>if the sum is lower than 5 AND iterations_number &lt; iterations_max_number, then continue iterations.</p>
</blockquote>
