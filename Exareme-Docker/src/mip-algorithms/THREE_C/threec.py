#!/usr/bin/env python

from __future__ import print_function
from __future__ import division


import rpy2.robjects as robjects
from rpy2.robjects import pandas2ri
import pandas as pd

from mipframework import Algorithm, AlgorithmResult, TabularDataResource


FEATURESELECT_METHODS = {
    "Random Forest": "RF",
    "AIC": "AIC",
    "AIC with MSFDR": "AIC_MSFDR",
    "BIC": "BIC",
}
C2_NUMCLUSTERS_METHODS = {
    "Euclidean": "Euclidean",
    "Manhattan": "Manhattan",
    "Hierarchical Euclidean": "hclust_Euclidean",
    "Hierarchical Manhattan": "hclust_Manhattan",
}
C2_CLUSTERING_METHODS = {
    "Euclidean": "Euclidean",
    "Manhattan": "Manhattan",
    "Hierarchical Euclidean": "Heuclidean",
    "Hierarchical Manhattan": "Hmanhattan",
}
C3_CLASSIFICATION_METHODS = {
    "Random Forest": "RF",
    "Random Forest Downsampling": "RF_downsampling",
    "CART Information": "CART_information",
    "CART Gini": "CART_gini",
}


class ThreeC(Algorithm):
    def __init__(self, cli_args):
        super(ThreeC, self).__init__(__file__, cli_args, intercept=False)

    def local_pure(self):
        c2_feature_selection_method = self.parameters.c2_feature_selection_method
        c2_feature_selection_method = FEATURESELECT_METHODS[c2_feature_selection_method]
        c2_num_clusters_method = self.parameters.c2_num_clusters_method
        c2_num_clusters_method = C2_NUMCLUSTERS_METHODS[c2_num_clusters_method]
        c2_clustering_method = self.parameters.c2_clustering_method
        c2_clustering_method = C2_CLUSTERING_METHODS[c2_clustering_method]
        # =======================================================================
        # NOTE: number_of_clusters parameter default value doesn't work in R code
        # =======================================================================
        #  try:
        #      c2_num_clusters = int(self.parameters.c2_num_clusters)
        #      c2_num_clusters_expr = 'k={}'.format(c2_num_clusters)
        #  except ValueError:
        #      c2_num_clusters_expr = ''
        c3_feature_selection_method = self.parameters.c3_feature_selection_method
        c3_feature_selection_method = FEATURESELECT_METHODS[c3_feature_selection_method]
        c3_classification_method = self.parameters.c3_classification_method
        c3_classification_method = C3_CLASSIFICATION_METHODS[c3_classification_method]

        cm_names = self.parameters.x
        pb_names = self.parameters.y
        markers_and_biomarkers = self.data.full[cm_names + pb_names]
        diag_name = self.parameters.dx
        diagnosis = self.data.db.select_vars_from_data(
            [diag_name], self.parameters.dataset, self.parameters.filter
        )
        full_data = pd.concat([diagnosis, markers_and_biomarkers], axis=1)

        var_names = [diag_name] + cm_names + pb_names
        var_categories = ["DX"] + ["CM"] * len(cm_names) + ["PB"] * len(pb_names)
        full_metadata = pd.DataFrame(
            {"varName": var_names, "varCategory": var_categories}
        )

        r_data = pandas2ri.py2ri(full_data)
        r_md = pandas2ri.py2ri(full_metadata)
        robjects.globalenv["data"] = r_data
        robjects.globalenv["metadata"] = r_md

        define_r_funcs()

        robjects.r(
            """
            x <- get_xy_from_DATA_C2(data, metadata)$x
            y <- get_xy_from_DATA_C2(data, metadata)$y
            """
        )
        robjects.r(
            """
            C2_results <- C2(x,
            y,
            feature_selection_method="{fsm}",
            num_clusters_method="{ncm}",
            clustering_method="{cm}",
            plot.num.clus=TRUE,
            plot.clustering=TRUE,
            k=6
            )
            """.format(
                fsm=c2_feature_selection_method,
                ncm=c2_num_clusters_method,
                cm=c2_clustering_method,
                #  nc_expr=c2_num_clusters_expr
            )
        )
        robjects.r(
            """
            PBx <- get_PBx_from_DATA_C3(data, metadata)
            new_y <- C2_results[[3]]
            """
        )
        robjects.r(
            """
            C3_results <- C3(PBx = PBx,
            newy = new_y,
            feature_selection_method = "{fsm}",
            classification_method="{cm}"
            )
            result <- table(new_y, C3_results[[2]])
            """.format(
                fsm=c3_feature_selection_method, cm=c3_classification_method
            )
        )
        res = list(robjects.globalenv["result"])
        table_out = TabularDataResource(
            fields=[""] * len(res), data=[tuple(res)], title="3C result"
        )
        self.result = AlgorithmResult(
            raw_data=dict(), tables=[table_out], highcharts=[],
        )


def define_r_funcs():
    rdef_get_xy_from_DATA_C2()
    rdef_feature_selection()
    rdef_Feature_Selection_dummy_regressions()
    rdef_Feature_Selection_RF()
    rdef_Feature_Selection_BIC()
    rdef_MSFDR()
    rdef_Feature_Selection_AIC_MSFDR()
    rdef_Feature_Selection_AIC()
    rdef_FDR_selection()
    rdef_number_of_clusters()
    rdef_k_euclidean()
    rdef_k_manhattan()
    rdef_khclust_euc()
    rdef_khclust_man()
    rdef_clustering()
    rdef_cluster_euclidean()
    rdef_cluster_manhattan()
    rdef_hclust_euc()
    rdef_hclust_man()
    rdef_C2()
    rdef_get_PBx_from_DATA_C3()
    rdef_C3()
    rdef_classification_fun()
    rdef_RF_classify()
    rdef_RF_one_by_one()
    rdef_cart_function()


def rdef_get_xy_from_DATA_C2():
    robjects.r(
        """
        #' Title get_xy_from_DATA_C2
        #'
        #' @param DATA Full data matrix, includes all observations for all the variables
        #' @param META_DATA Need to have at least 2 columns, one with all variables name, another one which indicate 
        #' the type of each variable (CM, DX, PB)
        #'
        #' @return a list of important variables
        #'
        #' @export
        #'
        #' @examples
        #' # x <- get_xy_from_DATA_C2(DATA, META_DATA)[[1]]
        #' # y <- get_xy_from_DATA_C2(DATA, META_DATA)[[2]]
        get_xy_from_DATA_C2 <- function(DATA, META_DATA) {
          # DATA META_DATA
          x <- DATA[, META_DATA$varName[META_DATA$varCategory == "CM"]]
          y <- DATA[, META_DATA$varName[META_DATA$varCategory == "DX"]]
          list(x = x, y = y)
        }
        """
    )


# =================
# Feature Selection
# =================
def rdef_feature_selection():
    robjects.r(
        """
        #' Title Features Selection
        #'
        #' @param x Data matrix
        #' @param y Dependent variable
        #' @param method The method to be used for the feature selection: Random forest, AIC, AIC with MSFDR or BIC
        #' @param ... further arguments to be passed to or from other methods
        #'
        #' @return a list of important variables
        #' 
        #' @export
        #'
        #' @examples 
        #' # feature_selection(x, y, method='RF')
        #' # feature_selection(x[, 1:30], y, method='BIC')
        #' # feature_selection(x, y, method='FDR_screening')
        feature_selection <- function(x, y, method = "RF", ...) {
          if (method == "RF") {
            output <- Feature_Selection_dummy_regressions(x, y, Feature_Selection_RF, 
                                                          ...)  # ('...' : p)
          }
          if (method == "AIC_MSFDR") {
            output <- Feature_Selection_dummy_regressions(x, y, Feature_Selection_AIC_MSFDR, 
                                                          ...)  # ('...' : q, print.the.steps)
          }
          if (method == "BIC") {
            output <- Feature_Selection_dummy_regressions(x, y, Feature_Selection_BIC, 
                                                          ...)  # ('...' : nbest, nvmax, nmin, plot)
          }
          if (method == "AIC") {
            output <- Feature_Selection_dummy_regressions(x, y, Feature_Selection_AIC)
          }
          if (method == "FDR_screening") {
            output <- Feature_Selection_dummy_regressions(x, y, FDR_selection, 
                                                          ...)  # ('...' : q, eta)
          }
          if (method == "LASSO") {
            output <- Feature_Selection_dummy_regressions(x, y, LASSO_selection)
          }
          return(output)
        }
        """
    )


def rdef_Feature_Selection_dummy_regressions():
    robjects.r(
        """
        #' Finds a subset of variables based on all dummy regressions
        #' Title Feature Selection Dummy Regression
        #'
        #' @param x Data matrix
        #' @param y Dependent variable
        #' @param FUN Indicating which method to use for feature selection 
        #' @param ... further arguments to be passed to or from other methods
        #'
        #' @return a vector with the names of the important variables
        #' @export
        #'
        #' @examples 
        #' Feature_Selection_dummy_regressions(x, y, Feature_Selection_RF)
        #' 
        Feature_Selection_dummy_regressions <- function(x, y, FUN, ...) {
          
          u_y <- unique(y)
          selected_variables <- list()
          
          for (i in seq_along(u_y)) {
            dummy_y <- as.numeric(y == u_y[i])
            # FUN(x, y, ...)
            selected_variables[[i]] <- FUN(x, dummy_y, ...)
          }
          
          # Union of all selected variables
          unique(unlist(selected_variables))
        }
        """
    )


# =================================
# Feature Selection - sub-functions
# =================================

# ==============
# Random Forests
# ==============
def rdef_Feature_Selection_RF():
    robjects.r(
        """
        #' Title Feature Selection Using Random Forest
        #'
        #' @param x Data matrix
        #' @param y Categorial dependent variable (factor)
        #' @param p Precentage of the number of variables to be chosen from x. Default value is 0.1.
        #' @return list of p precentage of the variables chosen by their Gini importance index.  
        #'         
        #' @export
        #'
        #' @examples 
        #' # Feature_Selection_RF(x, y, p = 0.1)
        #' 
        Feature_Selection_RF <- function(x, y, p = 0.1) {
          library(randomForest)
          
          if (!is.factor(y)) {
            warning("y is not a factor - but was coerced into one.")
            y <- as.factor(y)
          }
          
          rf_DX_by_CM <- randomForest(y ~ ., data = x, importance = TRUE, proximity = TRUE)
          
          var_import <- importance(rf_DX_by_CM)[, "MeanDecreaseAccuracy"]
          m <- round(dim(x)[2] * p)  # We'll save just 10% of the variables, the precentage can be changed
          subset_vars <- sort(var_import, decreasing = TRUE)[1:m]  # Sort the variables by their Gini importance index
          important_var_RF <- names(subset_vars)
          
          return(unlist(important_var_RF))
        }
    """
    )


# ===
# BIC
# ===
def rdef_Feature_Selection_BIC():
    robjects.r(
        """
        #' Title Feature Selection Using BIC
        #'
        #' @param x Data matrix
        #' @param y response vector (must be numeric?)
        #' @param nbest number of subsets of each size to record
        #' @param nvmax maximum size of subsets to examine
        #' @param nmin number of minimum varibles to be included in the suggested final model
        #' @param plot.BIC if TRUE (default) the function plots a table of models showing which variables are in each model.
        #'             The models are ordered by the specified model selection statistic.
        #' @return 
        #' vector with the names of variables of the model with minimum BIC between the models including more then 'nmin' variables' of regsubsets object
        #' @export 
        #'
        #' @examples 
        #' #  Feature_Selection_BIC(x[, 1:30], y, nbest=1, nvmax=5, plot.BIC=TRUE, nmin=4)
        Feature_Selection_BIC <- function(x, y, nbest = 1, nvmax = 12, nmin = 4, 
                                          plot.BIC = FALSE) {
          library(leaps)
          library(car)
          fulldata <- data.frame(x, y)  # Creating  one joint data.frame of the data
          RET <- regsubsets(y ~ ., data = fulldata, nbest = nbest, nvmax = nvmax, 
                            really.big = TRUE)
          # if (plot.BIC) { plot(RET, scale = 'bic') }
          summary_RET <- summary(RET)  # Saving the summary of the rugsubsets output
          help_mat <- matrix(as.numeric(summary_RET$which), nrow = (nvmax * nbest), 
                             ncol = (dim(x)[2] + 1))  # Which variables were chosen for each model
          num_var_each_model <- apply(help_mat, 1, sum)  # Counting the number of variables chosen for each model
          chosen_models <- summary_RET$bic[which(num_var_each_model >= nmin)]  # Saving the BIC value of the models which includes more then 'nmin' variables
          ind_model_min_BIC <- which(chosen_models == min(chosen_models))  # Which model with more then 3 variables have the minimum BIC 
          
          return(unlist(colnames(x)[which(help_mat[ind_model_min_BIC, ] == 1) - 
                                      1]))
        }
    """
    )


# ============
# AIC with FDR
# ============
def rdef_MSFDR():
    robjects.r(
        """
        #' Title Forward Selection Using AIC Criteria and MSFDR Procedure
        #'
        #' @param minimal.lm lm function output of model which includes an intercept
        #' @param maximal.lm lm function output of model which not includes an intercept
        #' @param q  Significant level. default as 0.05
        #' @param print.the.steps if TRUE the Lambda, model size, and final model  at each iteration will be printed;
        #'        Default as FALSE
        #' @param print.running.time  If TRUE the running time will be printed, it is equal to the value of print.the.steps
        #'        Default as False.
        #' @return 
        #' Final model, running time, summary of AIC_MSFDR object
        #' @export 
        #'
        #' @examples 
        #' # Feature_Selection_AIC_MSFDR(x, y, q = 0.5, print.the.steps = FALSE)
        #' 
        MSFDR <- function(minimal.lm, maximal.lm, q, print.the.steps, print.running.time = print.the.steps) {
          # computes forward model selection using the multiple stage FDR
          # controlling procedure (MSFDR)
          
          if (!(class(minimal.lm) == "lm" & class(maximal.lm) == "lm")) {
            print("one of the models you entered aren't linear models (lm), please try fitting lm only")
            break
          }
          
          if (print.running.time) 
            time <- proc.time()
          
          library(MASS)
          algorithm.direction <- "forward"  # always forward
          the.scope <- list(lower = minimal.lm, upper = maximal.lm)
          trace.stepAIC <- ifelse(print.the.steps, 1, 0)
          iteration.number <- 1
          
          m <- extractAIC(maximal.lm)[1] - 1  # check if the full model should include the intercept or not !!!!!!
          i <- max(extractAIC(minimal.lm)[1] - 1, 1)  # so if the model is with intercept only, the i size won't be 0.
          # q = .05 # default
          
          Lambda <- qnorm((1 - 0.5 * q * i/(m + 1 - i * (1 - q))))^2
          
          if (print.the.steps) {
            print(paste("Starting Lambda is: ", Lambda))
          }
          
          # first step of the algorithm
          new.lm <- stepAIC(minimal.lm, direction = algorithm.direction, scope = the.scope, 
                            k = Lambda, trace = trace.stepAIC)
          new.lm.model.size <- extractAIC(new.lm)[1] - 1
          
          
          while (new.lm.model.size > i) {
            iteration.number <- iteration.number + 1
            
            if (print.the.steps) {
              print("=========================================")
              print("=========================================")
              print(paste("iteration number: ", iteration.number))
              print(paste("current model size is:", new.lm.model.size, ">", 
                          i, " (which is bigger then the old model size)"))
            }
            
            i <- new.lm.model.size
            Lambda <- qnorm((1 - 0.5 * q * i/(m + 1 - i * (1 - q))))^2
            
            if (print.the.steps) {
              print(paste("new Lambda is: ", Lambda))
            }
            
            new.lm <- stepAIC(new.lm, direction = algorithm.direction, scope = the.scope, 
                              k = Lambda, trace = trace.stepAIC)
            
            new.lm.model.size <- extractAIC(new.lm)[1] - 1
          }
          
          
          if (print.the.steps) {
            print("=========================================")
            print("=========================================")
            print("=========================================")
            print("The final model is: ")
            print(new.lm$call)
          }
          
          if (print.running.time) {
            print("")
            print("Algorithm running time was:")
            print(proc.time() - time)
          }
          
          return(new.lm)
          
        }
    """
    )


def rdef_Feature_Selection_AIC_MSFDR():
    robjects.r(
        """
        # TODO: MSFDR does NOT (!!!) work with non-numeric values. Using it for
        # factors, will produce very wrong results It should be considered if
        # to extend it to also work with factors (e.g.: through multinomial
        # regression)
        Feature_Selection_AIC_MSFDR <- function(x, y, q = 0.05, print.the.steps = FALSE) {
          y <- as.numeric(y)
          fulldata <- data.frame(x, y = y)
          # Creating one joint data.frame of the data defining the smallest and
          # largest lm we wish to progress through
          smallest_linear_model <- lm(y ~ +1, data = fulldata)
          largest_linear_model <- lm(y ~ ., data = fulldata)
          
          # Implementing the MSFDR functions (with q = 0.05)
          AIC_MSDFR <- MSFDR(minimal.lm = smallest_linear_model, maximal.lm = largest_linear_model, 
                             q, print.the.steps)
          sum <- summary(AIC_MSDFR)  # Saving the summary of the AIC.MSFDR procedure
          important_var_FDR <- which(!is.na(AIC_MSDFR$coeff))
          important_var_FDR <- names(important_var_FDR)
          
          return(unlist(important_var_FDR[2:length(important_var_FDR)]))
        }
    """
    )


# ===================
# AIC without FDR ###
# ===================
def rdef_Feature_Selection_AIC():
    robjects.r(
        """
        #' Title Feature Selection Using AIC
        #'
        #' @param x data matrix
        #' @param y categorical variable (factor)
        #'
        #' @return
        #' Returns a list with two items. The first is a list of important variables. The second
        #' is NA if print.summary.AIC==FALSE or the summary of AIC if TRUE.
        #' @export
        #'
        #' @examples 
        #' # Feature_Selection_AIC(x, y)
        Feature_Selection_AIC <- function(x, y) {
          library(MASS)
          y <- as.numeric(y)
          fulldata <- data.frame(x, y)  # Creating  one joint data.frame of the data 
          smallest_linear_model <- lm(y ~ +1, data = fulldata)
          largest_linear_model <- lm(y ~ . + 1, data = fulldata)
          
          AIC_procedure <- stepAIC(object = smallest_linear_model, scope = list(lower = smallest_linear_model, 
                                                                                upper = largest_linear_model), direction = "forward", trace = FALSE)
          important_var_AIC <- names(AIC_procedure$coeff)
          
          return(unlist(important_var_AIC[2:length(important_var_AIC)]))  # Extracting the print of 'Intercept'
        }
    """
    )


# ==================================
# FDR Selection (F and Chi-sq tests)
# ==================================
def rdef_FDR_selection():
    robjects.r(
        """
    #' Title Feature Selection Using FDR selection
    #'
    #' @param x data matrix
    #' @param y categorical variable (factor)
    #' @param q adjusted p value threshold level. The chosen variables will have adjusted p value smaller than q
    #' @param eta eta squared threshold, the chosen variables will have eta value greater then eta. 
    #'
    #' @return
    #' Returns a list of the selected variables
    #' @export
    #'
    #' @examples 
    #' # FDR_selection(x, y, q = 0.001, eta = 0.1)
    FDR_selection <- function(x, y, q = 0.05, eta = 0.1) {
      
      if (!is.factor(y)) {
        warning("y is not a factor - but was coerced into one.")
        y <- as.factor(y)
      }
      
      eta_squared    <- rep(NA, dim(x)[2])
      original_p_val <- rep(NA, dim(x)[2])
      for (i in 1:dim(x)[2]) {
        # variable is discrete
        if (sum(floor(x[, i]) == x[, i]) == dim(x)[2]) 
        {
          original_p_val[i] <- chisq.test(x = x[, i], y)$p.value
          eta_squared[i] <- summary.lm(lm(as.factor(x[, i]) ~ as.factor(y)))$r.squared
        }  # variable is not discrete
        else {
          anova_model <- anova(lm(x[, i] ~ y + 0))
          original_p_val[i] <- anova_model[[5]][1]
          eta_squared[i] <- summary.lm(lm(x[, i] ~ as.factor(y)))$r.squared
        }
      }
      names(original_p_val) <- colnames(x)
      adjust_p_val <- p.adjust(original_p_val, method = "BH")
      
      is_smaller <- ifelse(adjust_p_val < q & eta_squared > eta, 1, 0)
      screening <- data.frame("var" = names(original_p_val), original_p_val, adjust_p_val,
                              eta_squared, is_smaller, row.names = c(1:length(original_p_val)))
      keep_vars <- screening$var[which(is_smaller == 1)]
      screening <- screening[order(original_p_val), ]
      
      
      return(as.character(keep_vars))
    }
    #' Title LASSO
    #'
    #' @param x Data matrix
    #' @param y Dependent variable
    #'
    #' @return 
    #' plot and table which advises how many clusters should be
    #' 
    #' @export
    #'
    #' @examples 
    #' # LASSO_selection(x, y)
    # LASSO_selection<-function(x, y) { cvfit <- cv.glmnet(as.matrix(x), y)
    # important_var_LASSO <- as.matrix(coef(cvfit, s = 'lambda.1se'))
    # important_var_LASSO <- important_var_LASSO[important_var_LASSO[, 1]
    # != 0, ] important_var_LASSO <-
    # important_var_LASSO[names(important_var_LASSO) != '(Intercept)']
    # reduced_x <- x[, names(important_var_LASSO)] return(reduced_x) }
    """
    )


# ======================================================
# Deciding on number of clusters and clustering the data
# ======================================================
def rdef_number_of_clusters():
    robjects.r(
        """
    #' Title Deciding on Number of Clusters
    #'
    #' @param x Data matrix
    #' @param method character string indicating how the "optimal" number of clusters: Euclidean (default), Manhattan, 
    #'        heirarchical euclidean or heirarchcal manhattan
    #' @param K.max the maximum number of clusters to consider, must be at least two. Default value is 10.
    #' @param B integer, number of Monte Carlo ("bootstrap") samples. Default value is 100.
    #' @param verbose integer or logical, determining if "progress" output should be printed. The default prints
    #'                one bit per bootstrap sample. Default value is FALSE.
    #' @param scale if TRUE (default) the data matrix will be scaled. 
    #' @param diss if TRUE (default as FALSE) x will be considered as a dissimilarity matrix. 
    #' @param cluster.only if true (default as FALSE) only the clustering will be computed and returned, see details.
    #' @param plot.num.clus if TRUE (default) the gap statistic plot will be printed
    #'
    #' @return 
    #' plot and table which advises how many clusters should be
    #' 
    #' @export
    #'
    #' @examples
    #' # number_of_clusters(subx, B=50, method='Euclidean')
    #' 
    number_of_clusters <- function(x, method = "Euclidean", K.max = 10, B = 100, 
                                   verbose = FALSE, plot.num.clus = TRUE, scale = TRUE, diss = FALSE, 
                                   cluster.only = TRUE) {
      # scale
      if (scale) {
        x <- scale(x)
      }
      
      # TODO: what we SHOULD do is pass Euclidean/Man to the functions, as
      # well as hclust vs pam...
      
      if (method == "Euclidean") {
        k_clusters <- k_euclidean(x, K.max, B, verbose, plot.num.clus)
      }
      if (method == "Manhattan") {
        k_clusters <- k_manhattan(x, K.max, diss, B, cluster.only, verbose, 
                                  plot.num.clus)
      }
      if (method == "hclust_Euclidean") {
        k_clusters <- khclust_euc(x, K.max, B, verbose, plot.num.clus)
        
      }
      if (method == "hclust_Manhattan") {
        k_clusters <- khclust_man(x, K.max, B, verbose, plot.num.clus)
        
      }
      return(list(k_clusters))
    }
    """
    )


def rdef_k_euclidean():
    robjects.r(
        """
    #' Title Gap statisic with k-medoids euclidean
    #'
    #' @param x Data matrix
    #' @param K.max the maximum number of clusters to consider, must be at least two. Default value is 10. 
    #' @param B integer, number of Monte Carlo ("bootstrap") samples. Default value is 100. 
    #' @param verbose integer or logical, determining if "progress" output should be printed. The default prints
    #'                 one bit per bootstrap sample. Default value is FALSE.
    #' @param plot.num.clus if TRUE (default) the gap statistic plot will be printed
    #'
    #' @return the clusGap function' values
    #' @export
    #'
    #' @examples
    #' # k_euclidean(subx, K.max=8, B=50, verbose=FALSE, plot.num.clus=TRUE)
    #' 
    k_euclidean <- function(x, K.max, B, verbose, plot.num.clus) {
      library(cluster)
      library(clusterCrit)
      
      clusGap_best <- cluster::clusGap(x, FUN = pam, K.max = K.max, B, verbose)
      
      
      if (plot.num.clus) {
        plot(clusGap_best, main = "Gap Statistic for k-medoids Euclidean")
      }
      # # Silhouette Criteria for k-medoids sil <- c(rep(NA, 10)) sil[1] <- 0
      # max_sil <- 0 clust_num_sil <- 0 for (i in 2:10) { clust <- pam(x, i,
      # diss = FALSE) sil[i] <- intCriteria(x, clust$cluster, 'Silhouette')
      # if (as.numeric(sil[i]) > max_sil) { max_sil_means <- sil[i]
      # clust_num_sil <- i } } if (plot.num.clus) { plot(as.numeric(sil),
      # type = 'l', main = 'Silhouette criteria k-medoids Euclidean') }
      
      # return(list(clusGap_best, clust))
      return(list(clusGap_best))
    }
    """
    )


def rdef_k_manhattan():
    robjects.r(
        """
    #' Title Gap statisic with k-medoids manhattan
    #'
    #' @param x data matrix
    #' @param K.max positive integer specifying the number of clusters, less than the number of observations. 
    #'              Default value is 10. 
    #' @param diss if TRUE (default as FALSE) x will be considered as a dissimilarity matrix        
    #' @param B integer, number of Monte Carlo ("bootstrap") samples. Default value is 100. 
    #' @param cluster.only  if true (default) only the clustering will be computed and returned, see details.
    #' @param verbose integer or logical, determining if "progress" output should be printed. The default prints
    #'                one bit per bootstrap sample. Default as FALSE. 
    #' @param plot.num.clus if TRUE (default) the gap statistic plot will be printed
    #' @param ... another objects of pam function
    #'
    #' @return clusGap function' output
    #' @export
    #'
    #' @examples
    #' #  k_manhattan (subx, K.max = 8, diss=FALSE, B = 50, cluster.only = TRUE, verbose = FALSE)
    #' 
    k_manhattan <- function(x, K.max, diss, B, cluster.only, verbose, plot.num.clus) {
      library(cluster)
      library(clusterCrit)
      library(magrittr)
      library(fpc)
      
      pam_1 <- function(x, k, ...) {
        clusters <- x %>% pam(k = k, diss = diss, metric = "manhattan", 
                              cluster.only = cluster.only)
        list(clusters = clusters)
      }
      set.seed(40)
      clusGap_best <- clusGap(x, FUN = pam_1, K.max = K.max, B = B, verbose = verbose)
      
      if (plot.num.clus) {
        
        plot(clusGap_best, main = "Gap Statistic for k-medoids Manhattan")
      }
      # #Silhouette criteria with k-medoids manhattan
      # sil_med_m<-c(rep(NA,10)) sil_med_m[1]<-0 max_sil_med_m<-0
      # clust_num_sil_med_m<-0 for (i in 2:10) {
      # clust_med_m<-pam(Scaled_Reduced_CM_trans,i,diss=FALSE,metric='manhattan')
      # sil_med_m[i]<-intCriteria(Scaled_Reduced_CM_trans,clust_med_m$cluster,'Silhouette')
      # if (as.numeric(sil_med_m[i]) > max_sil_med_m) {
      # max_sil_med_m<-sil_med_m[i] clust_num_sil_med_m<-i } }
      # plot(as.numeric(sil_med_m),type='l',main='Silhouette criteria,
      # k-medoids manhattan')
      return(list(clusGap_best))
    }
    """
    )


def rdef_khclust_euc():
    robjects.r(
        """
    #' Title  Gap statistics for hclust Euclidean
    #'
    #' @param x data matrix
    #' @param K.max positive integer specifying the number of clusters, less than the number of observations.
    #' @param B integer, number of Monte Carlo ("bootstrap") samples
    #' @param verbose integer or logical, determining if "progress" output should be printed. The default prints
    #'                   one bit per bootstrap sample
    #' @param plot.num.clus if TRUE (default) the gap statistic plot will be printed
    #'
    #' @return the clusGap function output
    #' @export
    #'
    #' @examples
    #' # khclust_euc(subx,K.max=10, B=60, verbose = FALSE, plot.num.clus=TRUE )
    #' 
    khclust_euc <- function(x, K.max, B, verbose, plot.num.clus) {
      hclust_k_euc <- function(x, k, ...) {
        library(magrittr)
        library(cluster)
        clusters <- x %>% dist %>% hclust %>% cutree(k = k)
        list(clusters = clusters)
      }
      
      clusGap_best <- clusGap(x, FUN = hclust_k_euc, K.max = K.max, B = B, 
                              verbose = verbose)
      if (plot.num.clus) {
        plot(clusGap_best, main = "Gap statistic, hclust Euclidean")
      }
      return(clusGap_best)
    }
    """
    )


def rdef_khclust_man():
    robjects.r(
        """
    #' Title Gap statistics for hclust Manhattan
    #'
    #' @param x data matrix
    #' @param K.max positive integer specifying the number of clusters, less than the number of observations.
    #' Default value is 10
    #' @param B integer, number of Monte Carlo ("bootstrap") samples. Default value is 100. 
    #' @param verbose integer or logical, determining if "progress" output should be printed. The default prints
    #'                   one bit per bootstrap sample. Default value is FALSE. 
    #' @param plot.num.clus if TRUE (default) the gap statistic plot will be printed
    #'
    #' @return the clusGap function output
    #' @export
    #'
    #' @examples
    #' # khclust_man(subx, K.max=8, B=60, verbose=FALSE, plot.num.clus=TRUE)
    #' 
    khclust_man <- function(x, K.max, B, verbose, plot.num.clus) {
      hclust_k_man <- function(x, k, ...) {
        library(magrittr)
        clusters <- x %>% dist(method = "manhattan") %>% hclust %>% cutree(k = k)
        list(clusters = clusters)
      }
      
      clusGap_best <- clusGap(x, FUN = hclust_k_man, K.max = K.max, B = B, 
                              verbose = verbose)
      if (plot.num.clus) {
        plot(clusGap_best, main = "Gap statistic, hclust Manhattan")
      }
      return(list(clusGap_best))
    }
    """
    )


# =====================
# Clustering the data #
# =====================
def rdef_clustering():
    robjects.r(
        """
    #' Title Clustering
    #'
    #' @param x data matrix
    #' @param k.gap positive integer specifying the number of clusters, less than the number of observation. Default value is 10.
    #' @param method Indicating which method to use for clustering. Default is 'Euclidean'. 
    #' @param plot.clustering if TRUE (default) a 2-dimensional "clusplot" plot will be printed
    #'
    #' @return vector withnew assigned clusters
    #' @export
    #'
    #' @examples
    #'  clustering(subx, k.gap = 5, method='Euclidean', plot.clustering=TRUE)
    #' 
    clustering <- function(x, k.gap = 2, method = "Euclidean", plot.clustering = FALSE) {
      
      if (method == "Euclidean") {
        clusters <- cluster_euclidean(x, k.gap, plot.clustering)
      }
      if (method == "Manhattan") {
        clusters <- cluster_manhattan(x, k.gap, plot.clustering)
      }
      if (method == "Heuclidean") {
        clusters <- cluster_euclidean(x, k.gap, plot.clustering)
      }
      if (method == "Hmanhattan") {
        clusters <- cluster_manhattan(x, k.gap, plot.clustering)
      }
      return(clusters)
    }
    ### Euclidean ###
    #' Title Clustering Using Euclidean distances
    #'
    #' @param x data matrix 
    #' @param k.gap positive integer specifying the number of clusters, less than the number of observation. Default value is 10.
    #' @param plot.clustering if TRUE (default) a 2-dimensional "clusplot" plot will be printed
    #'
    #' @return 
    #' vector with the new assigned clusters 
    #' 
    #' @export
    #'
    #' @examples 
    #' # cluster_euclidean(subx,  k.gap = 5, plot.clustering = TRUE)
    #' 
    """
    )


def rdef_cluster_euclidean():
    robjects.r(
        """
        # Title Cluster Euclidean
        cluster_euclidean <- function(x, k.gap, plot.clustering) {
          library(cluster)
          pam_4 <- pam(x, k.gap, diss = FALSE)
          if (plot.clustering) {
            clusplot(x, pam_4$cluster, color = TRUE, main = c("k-medoids,", 
                                                              paste = k.gap, "clusters"))
          }
          clusters <- pam_4$cluster
          
          return(unlist(clusters))
        }
    """
    )


# =========
# Manhattan
# =========
def rdef_cluster_manhattan():
    robjects.r(
        """
        #' Title Clustering Using Manhattan Distances
        #'
        #' @param x data matrix 
        #' @param k.gap positive integer specifying the number of clusters, less than the number of observation. Default value is 10.
        #' @param plot.clustering if TRUE (default) a 2-dimensional "clusplot" plot will be printed
        #'
        #' @return 
        #' vector with the new assigned clusters
        #' @export
        #'
        #' @examples
        #' # cluster_manhattan(subx, k.gap=4, plot.clustering=TRUE)
        #' 
        cluster_manhattan <- function(x, k.gap, plot.clustering) {
          pam_3_man <- pam(x, k.gap, diss = FALSE, metric = "manhattan")
          if (plot.clustering) {
            clusplot(x, pam_3_man$cluster, color = TRUE, main = c("k-medoids,manhattan", 
                                                                  paste(k.gap), "clusters"))
          }
          clusters <- pam_3_man$cluster
          
          return(unlist(clusters))
        }
    """
    )


def rdef_hclust_euc():
    robjects.r(
        """
        ### Hierarchical clustering euclidean ###
        #'  Title Deciding on number of clusters by using Hierarchical clustering euclidean
        #'
        #' @param x data matrix 
        #' @param y Dependent variable
        #' @param k.gap positive integer specifying the number of clusters, less than the number of observation. Default value is 10.
        #' @param plot.clustering if TRUE (default) a 2-dimensional "clusplot" plot will be printed
        #' 
        #'
        #' @return  
        #' summary table of the distribution to clusters 
        #' @export
        #'
        #' @examples
        #' hclust_euc(subx, k.gap = 5, plot.clustering=TRUE)
        #' 
        hclust_euc <- function(x, k.gap, plot.clustering) {
          d <- dist(x, method = "euclidean")
          fit_best <- hclust(d, method = "ward.D")
          if (plot.clustering) {
            plot(fit_best, main = c("hclust , euclidean,", paste(k.gap), " clusters"))
          }
          groups_best_4 <- cutree(fit_best, k = k.gap)
          rect.hclust(fit_best, k = k.gap, border = "blue")
          clusters <- groups_best_4
          return(unlist(clusters))
        }
    """
    )


# =================================
# Hierarchical clustering manhattan
# =================================
def rdef_hclust_man():
    robjects.r(
        """
        #' Title Deciding on number of clusters by Hierarchical clustering manhattan
        #'
        #' @param x data matrix 
        #' @param plot.clustering if TRUE (default) a 2-dimensional 'clusplot' plot will be printed
        #'
        #' @return 
        #' a list of two variables the hclust function description and a summary table
        #'  of the distribution to clusters
        #' @export
        #'
        #' @examples
        #' hclust_man(subx, k.gap = 5, plot.clustering=TRUE)
        #' 
        hclust_man <- function(x, k.gap, plot.clustering) {
          
          d_man <- dist(x, method = "manhattan")
          fit_best_man <- hclust(d_man, method = "ward.D")
          if (plot.clustering) {
            plot(fit_best_man, main = c("hclust, manhattan,", paste(k.gap), 
                                        "7 clusters"))
          }
          groups_best_4_man <- cutree(fit_best_man, k = k.gap)
          rect.hclust(fit_best_man, k = k.gap, border = "red")
          clusters <- groups_best_4_man
          
          return(unlist(clusters))
        }
    """
    )


# =============
# 3 C functions
# =============
def rdef_C2():
    robjects.r(
        """
    #' Title C2
    #'
    #' @param x data matrix 
    #' @param y Dependent variable
    #' @param feature_selection_method method for the feature selection of the clinical measurements stage. Default RF.
    #' @param num_clusters_method method for the choosing number of clusters by using the clinical measurements. Default Euclidean. 
    #' @param k number of clusters to use. If missing, we use a detection method. Defaukt as NULL
    #' @param clustering_method method for clustering using the reduced clinical measures. Default is Hmanhattan, 
    #'
    #' @return a list of three variables: 
    #' 1) vector with the names of the omportant variables chosen.
    #' 2) number of classes that will be used for clustering 
    #' 3) vector of the new assigned clusterst
    #' 
    #' @export
    #'
    #' @examples
    #' resultC2 <- C2(x, y, feature_selection_method='RF', num_clusters_method='Manhattan', clustering_method='Manhattan', plot.num.clus=TRUE, plot.clustering=TRUE)
    #' C2(x, y, feature_selection_method='BIC', num_clusters_method='Manhattan', clustering_method='Hmanhattan', plot.num.clus=TRUE, plot.clustering=FALSE, nbest=1, nvmax=8, B=50)
    C2 <- function(x, y, feature_selection_method, num_clusters_method, k = NULL, 
                   clustering_method, ...) {
      # Feature selection
      imp_var <- feature_selection(x, y, method = feature_selection_method)
      # print(imp_var) CM_final_vars <- imp_var[[1]][2] # Extracting a list
      # of inportant CM variables
      subx <- x[, unlist(imp_var)]
      # Deciding on number of clusters
      if (missing(k)) {
        num_clust <- number_of_clusters(x = subx, method = num_clusters_method)
        print(num_clust)
        # library(car)
        user_choise <- function() {
          k <- readline(prompt = paste("Enter the chosen number of clusters", 
                                       ":\n"))
          k <- as.numeric(k)
          return(k)
        }
        num_clust <- user_choise()
        
      } else {
        num_clust <- k
      }
      # Final clustering
      final_cluster <- clustering(subx, k.gap = num_clust)
      # print(final_cluster)
      return(list(imp_var, num_clust, final_cluster))
    }
    """
    )


def rdef_get_PBx_from_DATA_C3():
    robjects.r(
        """
    #' Title get_PBx_from_DATA_C3
    #' 
    #' @param DATA Full data matrix, includes all observations for all the variables
    #' @param META_DATA Need to have at least 2 columns, one with all variables name, another one which indicate 
    #' the type of each variable (CM, DX, PB)
    #'
    #' @return a list of important variables
    #' 
    #' @export
    #'
    #' @examples 
    #' # PBx <- get_PBx_from_DATA_C3(DATA, META_DATA)
    #' 
    get_PBx_from_DATA_C3 <- function(DATA, META_DATA) {
      x <- DATA[, META_DATA$varName[META_DATA$varCategory == "PB"]]
      return(PBx = x)
    }
    """
    )


def rdef_C3():
    robjects.r(
        """
    #' Title C3
    #'
    #' @param PBx data matrix 
    #' @param newy new assigned clusters, results from C2.
    #' @param feature_selection_method method for the feature selection of the Potential Bio-Markers
    #' @param classification_method method for classification  using the potential bio-markers
    #'
    #' @return a list of two variables: 
    #' 1) vector with the names of important variables chosen 
    #' 2) classification result for each observation 
    #' @export
    #'
    #' @examples
    #' C3(PBx, newy, feature_selection_method='RF', classification_method='RF') 
    #' 
    C3 <- function(PBx, newy, feature_selection_method, classification_method) {
      # Feature selection if(!factor(newy)){ newy <- as.factor(newy) }
      imp_var <- feature_selection(PBx, newy, method = feature_selection_method)
      sub_PBx <- PBx[, imp_var]
      # Classification
      classification <- classification_fun(PBx, newy, method = classification_method)
      return(list(imp_var, unname(classification)))
    }
    """
    )


def rdef_classification_fun():
    robjects.r(
        """
    ####################################### Potential biomarkers classification #
    #' Title Classification for the potential Biomarkers
    #'
    #' @param PBx data matrix 
    #' @param newy New assigned clusters
    #' @param method Classification method for the function to use
    #'
    #' @return Predicted values for each observation
    #' 
    #' @export
    #'
    #' @examples
    #' # classification_fun(PBx, newy, method='RF')
    classification_fun <- function(PBx, newy, method = "RF") {
      
      if (method == "RF") {
        output <- RF_classify(PBx, newy)
      }
      if (method == "RF_downsampling") {
        output <- RF_one_by_one(PBx, newy)
      }
      if (method == "CART_information") {
        output <- cart_function(PBx, newy, criteria = "information")
      }
      if (method == "CART_gini") {
        output <- cart_function(PBx, newy, criteria = "gini")
      }
      return(output)
    }
    """
    )


def rdef_RF_classify():
    robjects.r(
        """
    ### Random Forest Without Down Sampling ###
    #' Title Classification Using Random Forest Without Down Sampling
    #'
    #' @param PBx data matrix 
    #' @param newy New assigned clusters
    #'
    #' @return The predicted values for each observation 
    #'  
    #' @export
    #'
    #' @examples
    #' # RF_classify(PBx, newy)
    library(randomForest)
    RF_classify <- function(PBx, newy) {
      if (!is.factor(newy)) {
        warning("y is not a factor - but was coerced into one.")
        newy <- as.factor(newy)
      }
      fulldata <- data.frame(PBx, newy)
      rf_clus_PB <- randomForest(newy ~ ., data = fulldata, ntree = 50)
      model <<- rf_clus_PB 
      return(rf_clus_PB$predicted)
    }
    """
    )


def rdef_RF_one_by_one():
    robjects.r(
        """
    ### Random forest with down sampling ###
    #' Title Classification Using Random Forest Without Down Sampling
    #'
    #' @param PBx data matrix 
    #' @param newy New assigned clusters
    #'
    #' @return a list of two variables: the hclust function description and a summary table
    #'  of the distribution to clusters
    #' @export
    #'
    #' @examples
    #' # RF_one_by_one(PBx, newy)
    RF_one_by_one <- function(PBx, newy) {
      if (!is.factor(newy)) {
        warning("y is not a factor - but was coerced into one.")
        newy <- as.numeric(as.factor(newy))
      }
      rflist_names <- paste("cluster", c(1:length(unique(newy))))
      rflist <- sapply(rflist_names, function(x) NULL)
      for (i in 1:length(unique(newy))) {
        class_2 <- ifelse(newy == i, 1, 0)
        nmin <- sum(class_2 == 1)
        rflist[[i]] <- randomForest(factor(class_2) ~ ., data = PBx, ntree = 1000, 
                                    importance = TRUE, proximity = TRUE, sampsize = rep(nmin, 2))
      }
      return(rflist)
    }
    """
    )


def rdef_cart_function():
    robjects.r(
        """
    #' # cart_function(PBx, newy, 'information')
    ### CART ###
    #' Title Classification Using CART
    #'
    #' @param PBx data matrix 
    #' @param newy New assigned clusters
    #' @param criteria gini or information
    #'
    #' @return a list of two variables: the hclust function description and a summary table
    #'  of the distribution to clusters
    #' @export
    #'
    #' @examples
    cart_function <- function(PBx, newy, criteria = "gini") {
      
      fulldata <- data.frame(PBx, newy)
      cart <- rpart(newy ~ ., data = fulldata, method = "class", parms = list(split = criteria))
      model <<- cart 
      pred <- predict(cart, type = "class")
      return(pred)
      }
    """
    )


if __name__ == "__main__":
    import time
    from mipframework import create_runner

    algorithm_args = [
        "-y",
        "lefthippocampus, righthippocampus, leftcaudate",
        "-x",
        "apoe4, gender, agegroup",
        "-pathology",
        "dementia",
        "-dataset",
        "adni",
        "-filter",
        "",
        "-dx",
        "alzheimerbroadcategory",
        "-c2_feature_selection_method",
        "AIC",
        "-c2_num_clusters_method",
        "Euclidean",
        "-c2_num_clusters",
        "",
        "-c2_clustering_method",
        "Euclidean",
        "-c3_feature_selection_method",
        "Random Forest",
        "-c3_classification_method",
        "Random Forest",
    ]
    runner = create_runner(ThreeC, algorithm_args=algorithm_args, num_workers=1,)
    start = time.time()
    runner.run()
    end = time.time()
    print("Completed in ", end - start)
