from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import rpy2.robjects as robjects

from mipframework import Algorithm, AlgorithmResult

#  from mipframework.constants import P_VALUE_CUTOFF, P_VALUE_CUTOFF_STR, CONFIDENCE
#  from utils.algorithm_utils import ExaremeError


class ThreeC(Algorithm):
    def __init__(self, cli_args):
        super(ThreeC, self).__init__(__file__, cli_args, intercept=False)

    def local_pure(self):
        robjects.r(
            """
                library(CCC)

                data(c3_sample1)
                data(c3_sample1_categories)

                head(c3_sample1)

                table(c3_sample1_categories[,"varCategory"])

                x <- get_xy_from_DATA_C2(c3_sample1, c3_sample1_categories)$x
                y <- get_xy_from_DATA_C2(c3_sample1, c3_sample1_categories)$y

                C2_results <- C2(x, y, feature_selection_method="RF", num_clusters_method="Manhattan", clustering_method="M
                anhattan", plot.num.clus=TRUE, plot.clustering=TRUE, k=6)
                """
        )
        res = robjects.r(
            """
                C2_results

                PBx <- get_PBx_from_DATA_C3(c3_sample1, c3_sample1_categories)
                new_y <- C2_results[[3]]

                C3_results <- C3(PBx = PBx, newy = new_y, feature_selection_method = "RF", classification_method="RF")

                table(new_y, C3_results[[2]])
                """
        )
        res = list(res)
        raw_data = {"result": res}
        self.result = AlgorithmResult(raw_data=raw_data, tables=[], highcharts=[])


if __name__ == "__main__":
    import time
    from mipframework import create_runner

    algorithm_args = [
        "-y",
        "lefthippocampus",
        "-x",
        "righthippocampus",
        "-pathology",
        "dementia",
        "-dataset",
        "adni",
        "-filter",
        "",
        "-dx",
        "alzheimerbroadcategory",
        "-c2_feature_selection_method",
        "RF",
        "-c2_num_clusters_method",
        "Euclidean",
        "-c2_num_clusters",
        "",
        "-c2_clustering_method",
        "Euclidean",
        "-c3_feature_selection_method",
        "RF",
        "-c3_classification_method",
        "RF",
    ]
    runner = create_runner(ThreeC, algorithm_args=algorithm_args, num_workers=1,)
    start = time.time()
    runner.run()
    end = time.time()
    print("Completed in ", end - start)
