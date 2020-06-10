<b><h2><center>ANOVA</center></h1></b>

<b><h4> Some General Remarks </h4></b>
The general architecture of the MIP follows a Master/Worker paradigm where many Workers, operating in multiple medical centers, are coordinated by one Master. Only Workers are allowed access to the anonymized data in each medical center and the Master only sees aggregate data, derived from the full data and sent to him by the Workers.

As a consequence, every algorithm has to be refactored in a form that fits this model.

In general, this means two things.

1. On the one hand, isolating the parts of the algorithm that operate on the full data and implement them in procedures that run on Workers.  
2. On the other hand, identifying the parts of the algorithm that need to see the aggregates from all Workers and implementing these parts in procedures that run on Master.

Our naming convention is that procedures run on Workers are given the adjective _local_ whereas those running on Master are called _global_.

<b><h4> Sums of Squares Types </h4></b>
There are three different classical approaches for computing sums of squares (<img src="https://render.githubusercontent.com/render/math?math=SS">) and testing hypotheses in ANOVA for unbalanced data commonly called Type I, II, and III sums of squares.

Consider a model that includes two factors <img src="https://render.githubusercontent.com/render/math?math=A"> and <img src="https://render.githubusercontent.com/render/math?math=B">; there are therefore two main effects, and an interaction, <img src="https://render.githubusercontent.com/render/math?math=AB">. The full model is represented by <img src="https://render.githubusercontent.com/render/math?math=SS(A,B,AB)"> . Other models are represented similarly: <img src="https://render.githubusercontent.com/render/math?math=SS(A,B)"> indicates the model with no interaction, <img src="https://render.githubusercontent.com/render/math?math=SS(B,AB)"> indicates the model that does not account for main effects from factor <img src="https://render.githubusercontent.com/render/math?math=A">, and so on.

The influence of particular factors (including interactions) can be tested by examining the differences between models. For example, to determine the presence of an interaction effect, an F-test of the models <img src="https://render.githubusercontent.com/render/math?math=SS(A,B,AB)"> and the no-interaction model <img src="https://render.githubusercontent.com/render/math?math=SS(A,B)"> would be carried out.

It is handy to define incremental sums of squares to represent these differences. Let

<img src="https://render.githubusercontent.com/render/math?math=SS(AB|A,B)=SS(A,B,AB)-SS(A,B)">

<img src="https://render.githubusercontent.com/render/math?math=SS(A|B,AB)=SS(A,B,AB)-SS(B,AB)">

<img src="https://render.githubusercontent.com/render/math?math=SS(A|A,AB)=SS(A,B,AB)-SS(A,AB)">

<img src="https://render.githubusercontent.com/render/math?math=SS(A|B)=SS(A,B)-SS(B)">  

<img src="https://render.githubusercontent.com/render/math?math=SS(B|A)=SS(A,B)-SS(A)">  

The notation shows the incremental differences in sums of squares, for example <img src="https://render.githubusercontent.com/render/math?math=SS(AB|A,B)"> represents the sum of squares for interaction after the main effects, and  <img src="https://render.githubusercontent.com/render/math?math=SS(A|B)"> is the sum of squares for the <img src="https://render.githubusercontent.com/render/math?math=A">  main effect after the <img src="https://render.githubusercontent.com/render/math?math=B"> main effect and ignoring interactions.

The different types of sums of squares then arise depending on the stage of model reduction at which they are carried out. In particular:

<b><h5>Type I: sequential sum of squares</b></h5>

The Type I analysis corresponds to adding each effect sequentially to the model and it depends on how the model terms are ordered. Different orders may give quite different results.

Let <img src="https://render.githubusercontent.com/render/math?math=SS(A,B,AB)"> be the full model. We test:

<img src="https://render.githubusercontent.com/render/math?math=SS(A)"> for factor  <img src="https://render.githubusercontent.com/render/math?math=A">.

<img src="https://render.githubusercontent.com/render/math?math=SS(B|A)"> for factor  <img src="https://render.githubusercontent.com/render/math?math=B">.

<img src="https://render.githubusercontent.com/render/math?math=SS(AB|B,A)"> for interaction  <img src="https://render.githubusercontent.com/render/math?math=AB">.

<b><h5>Type II: hierarchical or partially sequential</b></h5>

Type II computes <img src="https://render.githubusercontent.com/render/math?math=SS">  for all effects in the model that are at the same or lower level. For example,<img src="https://render.githubusercontent.com/render/math?math=SS"> for the main effects take account of all other main effects, rather than simply accounting for those entered earlier in the model. Interaction effects take account of all main effects and all other interaction effects at the same level.

Let <img src="https://render.githubusercontent.com/render/math?math=SS(A,B,AB)"> be the full model. We test:

<img src="https://render.githubusercontent.com/render/math?math=SS(A|B)"> for factor  <img src="https://render.githubusercontent.com/render/math?math=A">.

<img src="https://render.githubusercontent.com/render/math?math=SS(B|A)"> for factor  <img src="https://render.githubusercontent.com/render/math?math=B">.

<img src="https://render.githubusercontent.com/render/math?math=SS(AB|B,A)"> for interaction  <img src="https://render.githubusercontent.com/render/math?math=AB">.

<b><h5>Type III: marginal or orthogonal</b></h5>

<img src="https://render.githubusercontent.com/render/math?math=SS"> gives the sum of squares that would be obtained for each variable if it were entered last into the model. That is, the effect of each variable is evaluated after all other factors have been accounted for. Therefore the result for each term is equivalent to what is obtained with Type I analysis when the term enters the model as the last one in the ordering.

Let <img src="https://render.githubusercontent.com/render/math?math=SS(A,B,AB)"> be the full model. We test:

<img src="https://render.githubusercontent.com/render/math?math=SS(A|B,AB)"> for factor  <img src="https://render.githubusercontent.com/render/math?math=A">.

<img src="https://render.githubusercontent.com/render/math?math=SS(B|A,AB)"> for factor  <img src="https://render.githubusercontent.com/render/math?math=B">.

<b><h4>Federated ANOVA</b></h4>

Based on the type of sum of squares selected by the user as well as the full model, a set of models is defined as described above. Federated linear regressions are executed for each of these models. Once the federated linear regressions are computed, we compute and output all the relevant statistics and p-values of standard ANOVA.
