from __future__ import print_function
from __future__ import unicode_literals
from __future__ import division

import random
from datetime import timedelta

from dateutil.relativedelta import relativedelta
from collections import namedtuple
import numpy as np
import pandas as pd

from faker import Faker
from tqdm import tqdm

PatientRecord = namedtuple(
    "PatientRecord",
    "subjectcode subjectage subjectvisitid subjectvisitdate "
    "alzheimerbroadcategory apoe4 dataset",
)


def alzheimer_oracle(apoe4):
    prob = None
    if apoe4 == 0:
        prob = 0.8
    elif apoe4 == 1:
        prob = 0.5
    elif apoe4 == 2:
        prob = 0.24
    r = np.random.random()
    return True if r <= prob else False


def get_age(birth_date, visit):
    age = relativedelta(visit, birth_date)
    age = age.years + age.months / 12
    return age


def get_visits(fake, birth_date):
    num_visits = random.randint(3, 15)
    first_visit = fake.date_between(
        start_date=birth_date + timedelta(days=64 * 365),
        end_date=birth_date + timedelta(days=65 * 365),
    )
    visits = sorted(
        [
            fake.date_between(
                start_date=first_visit, end_date=first_visit + timedelta(days=3 * 365)
            )
            for _ in range(num_visits)
        ]
    )
    return visits


def patients():
    fake = Faker()
    subject_code = fake.md5()
    birth_date = fake.date_of_birth(minimum_age=75, maximum_age=90)
    visits = get_visits(fake, birth_date)
    apoe4 = np.random.choice([0, 1, 2], p=[0.03, 0.17, 0.8])
    can_get_sick = alzheimer_oracle(apoe4)
    alzheimerbroadcategory = "MCI"
    for i, visit in enumerate(visits):
        visit_id = fake.md5()
        age = get_age(birth_date, visit)
        if can_get_sick and age > 64 and alzheimerbroadcategory == "MCI":
            prob = 2 ** ((age - 6) // 2) * 0.3
            r = np.random.random()
            if r <= prob:
                alzheimerbroadcategory = "AD"

        yield PatientRecord(
            subject_code,
            age,
            visit_id,
            visit.strftime("%Y-%m-%d") + " 0:00",
            alzheimerbroadcategory,
            apoe4,
            "alzheimer_fake_cohort",
        )


def cohort(num_patients):
    with tqdm(total=num_patients, desc="Generating fake cohort") as pbar:
        for _ in range(num_patients):
            for visit in patients():
                yield visit
            pbar.update(1)


def main():
    num_patients = 2000
    data = pd.DataFrame(cohort(num_patients))
    final = [g[1].iloc[-1].alzheimerbroadcategory for g in data.groupby("subjectcode")]
    print(sum(1 for f in final if f == "AD") / num_patients)
    data = data.set_index("subjectcode")
    data.to_csv("alzheimer_fake_cohort.csv")


if __name__ == "__main__":
    main()
