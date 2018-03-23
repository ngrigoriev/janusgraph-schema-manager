#!/bin/sh

CMD="java -cp ../target/*:../target/dependencies/*  com.newforma.titan.schema.SchemaManagerApp -g ../examples/sandbox/graph-config/dynamodb-local.properties"

IMGDIR=images

TMPDIR=/tmp/schema-docs-$$
mkdir $TMPDIR 
if [ $? != 0 ] ; then
	echo "Unable to create $TMPDIR"
	exit 1
fi

trap "rm -rf $TMPDIR" EXIT


function gen_schema_image() {
	local schema_file=$1
	local tags=$2
	local imgfile=$3
	rm -f $TMPDIR/*.{xml,dot,css,xsl}
	$CMD -t "$tags" -d $TMPDIR $schema_file
	dot -Tpng $TMPDIR/graph.dot > $imgfile
}


# GraphViz
GV_SCHEMA=samples/graphviz_demo.json
gen_schema_image $GV_SCHEMA "all,normal_edge" $IMGDIR/graphviz_normal_edge.png
gen_schema_image $GV_SCHEMA "all,two_way_edge" $IMGDIR/graphviz_normal_2way_edge.png
gen_schema_image $GV_SCHEMA "all,1to1_edge" $IMGDIR/graphviz_1to1_edge.png
gen_schema_image $GV_SCHEMA "all,invisible_edge" $IMGDIR/graphviz_invisible_edge.png
gen_schema_image $GV_SCHEMA "all,unidir_edge" $IMGDIR/graphviz_unidir_edge.png
gen_schema_image $GV_SCHEMA "all,unidir_inv_edge" $IMGDIR/graphviz_unidir_inv_edge.png
gen_schema_image $GV_SCHEMA "all,nonexist_inv_edge" $IMGDIR/graphviz_nonexist_inv.png
gen_schema_image $GV_SCHEMA "all,vertexd" $IMGDIR/graphviz_nonexist_edge.png
gen_schema_image $GV_SCHEMA "vertexe" $IMGDIR/graphviz_nonexist_edge_inv.png
gen_schema_image $GV_SCHEMA "vertexg,vertexa,edgega" $IMGDIR/graphviz_static_vertex.png

cp $TMPDIR/graph.dot /tmp/

rm -rf $TMPDIR
