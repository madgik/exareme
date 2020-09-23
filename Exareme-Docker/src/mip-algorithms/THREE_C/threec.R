library(CCC)

data(c3_sample1)
data(c3_sample1_categories)

x <- get_xy_from_DATA_C2(c3_sample1, c3_sample1_categories)$x
y <- get_xy_from_DATA_C2(c3_sample1, c3_sample1_categories)$y

C2_results <- C2(x, y, feature_selection_method="RF", num_clusters_method="Manhattan", clustering_method="Manhattan", plot.num.clus=TRUE, plot.clustering=TRUE, k=6)

C2_results

PBx <- get_PBx_from_DATA_C3(c3_sample1, c3_sample1_categories)
new_y <- C2_results[[3]]

C3_results <- C3(PBx = PBx, newy = new_y, feature_selection_method = "RF", classification_method="RF") 

table(new_y, C3_results[[2]])
