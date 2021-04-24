##Steps to run the answer module in a standalone environment

#Setup virtual environment
sudo apt-get update
sudo apt-get install python-pip
sudo apt-get install python3-venv
python3 -m venv nlp/env
source nlp/env/bin/activate

#Setting up docker
sudo apt-get install \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo \
  "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

 
sudo apt install apt-transport-https ca-certificates curl software-properties-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu bionic test"
sudo apt update
 sudo apt-get install docker-ce docker-ce-cli containerd.io

#Install dependencies
pip3 install --upgrade pip
pip3 install torch
pip3 install pycorenlp 
pip3 install requests
pip3 install nltk
pip3 install transformers

#Pull corenlp server docker image
sudo docker pull graham3333/corenlp-complete
sudo docker run -itd -p 9000:9000 --name corenlp graham3333/corenlp-complete

#Run the answer program
./answer article2.txt questions2.txt