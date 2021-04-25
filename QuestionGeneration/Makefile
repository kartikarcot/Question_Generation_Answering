ask:
	SHELL:=/bin/bash
ask:
	bash -c "ls"
	bash -c "javac -cp $$(printf %s: ../nlp_project/stanford-corenlp-4.2.0/*.jar) ./CoreImplementation/*.java -d ./out_test/"
	#javac -cp "../nlp_project/stanford-corenlp-4.2.0/stanford-corenlp-4.2.0.jar:../nlp_project/stanford-corenlp-4.2.0/stanford-corenlp-4.2.0-models.jar" ./CoreImplementation/*.java -d ./out_test/
	cp ../nlp_project/stanford-corenlp-4.2.0/*.jar ./out_test/
	for f in ./out_test/*.jar; do tar xf "$$f" -C ./out_test/ ; done
	rm ./out_test/*.jar
	jar cvfm ask.jar CoreImplementation/META-INF/MANIFEST.MF -C ./out_test/ .

clean:
	rm -r ./out_test
	rm ./ask.jar