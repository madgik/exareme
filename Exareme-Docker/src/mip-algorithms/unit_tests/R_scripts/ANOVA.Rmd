library(base)
library(tidyverse)
library(jmv)
library(jsonlite)
library(dplyr)
library(tidyr)


# LOAD BALANCED DATA
rawDataB <- read_delim("../unit_tests/datasets/CSVs/data_ANOVA_Balanced_with_inter_V1V2.csv", ",") %>%
  mutate(
    var_I1 = as.factor(var_I1),
    var_I2 = as.factor(var_I2),
    var_I3 = as.factor(var_I3)
    )

# TEST 1
Test1Info <- "Balanced data set, test the 3 main effects and all the interactions with type I ANOVA "

anovaResult <- jmv::ANOVA( formula   = var_D ~ var_I1*var_I2*var_I3,
                           data      = rawDataB,
                           ss        = 1,
                           effectSize = c('eta', 'partEta'))

Test1Result <-toJSON(anovaResult$main$asDF)

# TEST 2
Test2Info <- "Balanced data set, test the 3 main effects and all the interactions with type II ANOVA "

anovaResult <- jmv::ANOVA( formula   = var_D ~ var_I1*var_I2*var_I3,
                           data      = rawDataB,
                           ss        = 2,
                           effectSize = c('eta', 'partEta'))

Test2Result <-toJSON(anovaResult$main$asDF)

# TEST 3
Test3Info <- "Balanced data set, test the 3 main effects and all the interactions with type II ANOVA "

anovaResult <- jmv::ANOVA( formula   = var_D ~ var_I1*var_I2*var_I3,
                           data      = rawDataB,
                           ss        = 3,
                           effectSize = c('eta', 'partEta'))

Test3Result <-toJSON(anovaResult$main$asDF)

####################################################################################################################################
#IMPORT UNBALANCED DATA
rawDataUB <- read_delim("../unit_tests/datasets/CSVs/data_ANOVA_Unbalanced_with_inter_V1V2.csv", ",")
rawDataUB <- rawDataUB %>%
  mutate(
    var_I1 = as.factor(var_I1),
    var_I2 = as.factor(var_I2),
    var_I3 = as.factor(var_I3)
    )

# TEST 4
Test4Info <- "Unbalanced data set, test the 3 main effects and all the interactions with type I ANOVA "

anovaResult <- jmv::ANOVA(  formula   = var_D ~ var_I1*var_I2*var_I3,
                            data      = rawDataUB,
                            ss        = 1,
                            effectSize = c('eta', 'partEta'))

Test4Result <-toJSON(anovaResult$main$asDF)

# TEST 5
Test5Info <- "Unbalanced data set, test the 3 main effects and all the interactions with type II ANOVA "

anovaResult <- jmv::ANOVA(  formula   = var_D ~ var_I1*var_I2*var_I3,
                            data      = rawDataUB,
                            ss        = 2,
                            effectSize = c('eta', 'partEta'))

Test5Result <-toJSON(anovaResult$main$asDF)


# TEST 6
Test6Info <- "Unbalanced data set, test the 3 main effects and all the interactions with type III ANOVA "

anovaResult <- jmv::ANOVA(  formula   = var_D ~ var_I1*var_I2*var_I3,
                            data      = rawDataUB,
                            ss        = 3,
                            effectSize = c('eta', 'partEta'))

Test6Result <-toJSON(anovaResult$main$asDF)


# TEST 7
Test7Info <- "Unbalanced data set, test the 3 main effects only with type III ANOVA "

anovaResult <- jmv::ANOVA(  formula   = var_D ~ var_I1 + var_I2 + var_I3,
                            data      = rawDataUB,
                            ss        = 3,
                            effectSize = c('eta', 'partEta'))

Test7Result <-toJSON(anovaResult$main$asDF)

####################################################################################################################################
#With data similar to our target
df0 <- read_csv("../unit_tests/datasets/CSVs/dataset_0.csv") %>%
  mutate(
    agegroup                = as.ordered(agegroup),
    alzheimerbroadcategory  = as.factor(alzheimerbroadcategory),
    dataset                 = as.factor(dataset),
    gender                  = as.factor(gender)
    )



# TEST 8
Test8Info <- "With data similar to our target, without interaction and 2 variables - type III"

anovaResult <- jmv::ANOVA(  formula   = lefthippocampus ~ alzheimerbroadcategory + gender,
                            data      = df0,
                            ss        = 3,
                            effectSize = c('eta', 'partEta'))

Test8Result <-toJSON(anovaResult$main$asDF)


# TEST 9
Test9Info <- "With data similar to our target, without interaction and 2 variables - type II"

anovaResult <- jmv::ANOVA(  formula   = lefthippocampus ~ alzheimerbroadcategory + gender,
                            data      = df0,
                            ss        = 2,
                            effectSize = c('eta', 'partEta'))

Test9Result <-toJSON(anovaResult$main$asDF)


# TEST 10
Test10Info <- "With data similar to our target, with interaction and 2 variables - type III"

anovaResult <- jmv::ANOVA(  formula   = lefthippocampus ~ alzheimerbroadcategory * gender,
                            data      = df0,
                            ss        = 3,
                            effectSize = c('eta', 'partEta'))

Test10Result <-toJSON(anovaResult$main$asDF)


# TEST 11
Test11Info <- "With data similar to our target, with interaction and 2 variables - type II"

anovaResult <- jmv::ANOVA(  formula   = lefthippocampus ~ alzheimerbroadcategory * gender,
                            data      = df0,
                            ss        = 2,
                            effectSize = c('eta', 'partEta'))

Test11Result <-toJSON(anovaResult$main$asDF)


# TEST 12
Test12Info <- "With data similar to our target, without interaction and 3 variables - type III"

anovaResult <- jmv::ANOVA(  formula   = lefthippocampus ~  alzheimerbroadcategory + gender + agegroup,
                            data      = df0,
                            ss        = 3,
                            effectSize = c('eta', 'partEta'))

Test12Result <-toJSON(anovaResult$main$asDF)



# TEST 13
Test13Info <- "With data similar to our target, without interaction and 3 variables - type II"

anovaResult <- jmv::ANOVA(  formula   = lefthippocampus ~  alzheimerbroadcategory + gender + agegroup,
                            data      = df0,
                            ss        = 2,
                            effectSize = c('eta', 'partEta'))

Test13Result <-toJSON(anovaResult$main$asDF)



# TEST 14
Test14Info <- "With data similar to our target, with 1 interaction and 3 variables - type III"

anovaResult <- jmv::ANOVA(  formula   = lefthippocampus ~  alzheimerbroadcategory * gender + agegroup,
                            data      = df0,
                            ss        = 3,
                            effectSize = c('eta', 'partEta'))

Test14Result <-toJSON(anovaResult$main$asDF)


# TEST 15
Test15Info <- "With data similar to our target, with 1 interaction and 3 variables - type II"

anovaResult <- jmv::ANOVA(  formula   = lefthippocampus ~  alzheimerbroadcategory * gender + agegroup,
                            data      = df0,
                            ss        = 2,
                            effectSize = c('eta', 'partEta'))

Test15Result <-toJSON(anovaResult$main$asDF)


# TEST 16
Test16Info <- "With data similar to our target, with full interaction and 3 variables - type III"

anovaResult <- jmv::ANOVA(  formula   = lefthippocampus ~  alzheimerbroadcategory * gender * agegroup,
                            data      = df0,
                            ss        = 3,
                            effectSize = c('eta', 'partEta'))

Test16Result <-toJSON(anovaResult$main$asDF)


# TEST 17
Test17Info <- "With data similar to our target, with full interaction and 3 variables - type II"

anovaResult <- jmv::ANOVA(  formula   = lefthippocampus ~  alzheimerbroadcategory * gender * agegroup,
                            data      = df0,
                            ss        = 2,
                            effectSize = c('eta', 'partEta'))

Test17Result <-toJSON(anovaResult$main$asDF)


x <- c(Test1Info, Test2Info, Test3Info, Test4Info, Test5Info, Test6Info, Test7Info, Test8Info, Test9Info, Test10Info,
       Test11Info, Test12Info, Test13Info, Test14Info, Test15Info, Test16Info, Test17Info);
y <- c(Test1Result, Test2Result, Test3Result, Test4Result, Test5Result,Test6Result, Test7Result, Test8Result, Test9Result, Test10Result,
       Test11Result, Test12Result, Test13Result, Test14Result, Test15Result,Test16Result, Test17Result)

A<-c(x,y); dim(A) <- c(17,2)

return(y)
