docker build -t central .
docker tag central:latest 344855247956.dkr.ecr.us-east-2.amazonaws.com/central:latest
docker push 344855247956.dkr.ecr.us-east-2.amazonaws.com/central:latest