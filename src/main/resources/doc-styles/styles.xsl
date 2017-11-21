<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml">

    <xsl:output method="html" doctype-system="about:legacy-compat" />

    <xsl:variable name="java_api_javadoc_url_base">https://docs.oracle.com/javase/8/docs/api</xsl:variable>
    <xsl:variable name="titan_api_javadoc_url_base">http://titan.thinkaurelius.com/javadoc/1.0.0</xsl:variable>
    <xsl:variable name="tinkerpop_api_javadoc_url_base">http://tinkerpop.apache.org/javadocs/3.0.1-incubating/full</xsl:variable>
    <xsl:variable name="allinone" select="count(/doc-root/doc-meta[text() = 'allinone']) > 0"/>
    <xsl:variable name="vertex_doc_file">
    	<xsl:choose>
    		<xsl:when test="$allinone"/>
    		<xsl:otherwise>vertices.xml</xsl:otherwise>
    	</xsl:choose>
   	</xsl:variable>
    <xsl:variable name="edge_doc_file">
    	<xsl:choose>
    		<xsl:when test="$allinone"/>
    		<xsl:otherwise>edges.xml</xsl:otherwise>
    	</xsl:choose>
    </xsl:variable>
    <xsl:variable name="property_doc_file">
    	<xsl:choose>
    		<xsl:when test="$allinone"/>
    		<xsl:otherwise>properties.xml</xsl:otherwise>
    	</xsl:choose>
    </xsl:variable>
    <xsl:variable name="index_doc_file">
    	<xsl:choose>
    		<xsl:when test="$allinone"/>
    		<xsl:otherwise>graph-indexes.xml</xsl:otherwise>
    	</xsl:choose>
    </xsl:variable>
    <xsl:variable name="local_edge_index_file">
    	<xsl:choose>
    		<xsl:when test="$allinone"/>
    		<xsl:otherwise>local-edge-indexes.xml</xsl:otherwise>
    	</xsl:choose>
    </xsl:variable>
    <xsl:variable name="local_property_index_file">
    	<xsl:choose>
    		<xsl:when test="$allinone"/>
    		<xsl:otherwise>local-property-indexes.xml</xsl:otherwise>
    	</xsl:choose>
    </xsl:variable>
    <xsl:variable name="main_doc_file">
    	<xsl:choose>
    		<xsl:when test="$allinone"/>
    		<xsl:otherwise>index.xml</xsl:otherwise>
    	</xsl:choose>
    </xsl:variable>



    <xsl:variable name="property_a_prefix">prop_</xsl:variable>
    <xsl:variable name="edge_a_prefix">edge_</xsl:variable>
    <xsl:variable name="vertex_a_prefix">vertex_</xsl:variable>


	<xsl:template match="/doc-root">
        <html>
            <head>
            	<link type="text/css" rel="stylesheet" href="styles.css"/>
                <meta charset="utf-8" />
                <title>Graph schema documentation - <xsl:value-of select="name/text()"/></title>
            </head>
            <body>
		  		<xsl:apply-templates/>
		  	</body>
	  	</html>
	</xsl:template>

    <xsl:template match="schema-meta[count(/doc-root/child::*) = 1] | schema-meta[count(/doc-root/doc-meta[text() = 'allinone']) > 0]">
	    <h1 class="title">Graph schema documentation - <xsl:value-of select="name/text()"/></h1>

	    <h2 class="title">Graph information</h2>

	    <table class="graph-meta">
	    	<tbody>
	    		<tr>
	    			<td class="param graph_name">Graph name</td>
	    			<td class="value graph_name"><xsl:value-of select="name/text()"/></td>
	    		</tr>
	    		<tr>
	    			<td class="param model_version">Model version</td>
	    			<td class="value model_version"><xsl:value-of select="model_version/text()"/></td>
	    		</tr>
	    	</tbody>
	    </table>

	    <h2 class="title">Graph relationship naming conventions</h2>

		<table class="graph-naming">
			<tbody>
				<tr>
					<td class="param vertex_label_regex">Vertex label regex</td>
					<td class="value vertex_label_regex regex">
						<xsl:value-of select="conventions/vertexLabelRegex/text()"/>
					</td>
				</tr>
				<tr>
					<td class="param edge_label_regex">Edge label regex</td>
					<td class="value edge_label_regex regex">
						<xsl:value-of select="conventions/edgeLabelRegex/text()"/>
					</td>
				</tr>
				<tr>
					<td class="param prop_label_regex">Property label regex</td>
					<td class="value prop_label_regex regex">
						<xsl:value-of select="conventions/propertyKeyRegex/text()"/>
					</td>
				</tr>
				<tr>
					<td class="param index_name_regex">Index label regex</td>
					<td class="value index_name_regex regex">
						<xsl:value-of select="conventions/indexNameRegex/text()"/>
					</td>
				</tr>
			</tbody>
		</table>

		<xsl:if test="indexing">
		<h2 class="title">Graph indexing defaults</h2>
			<table class="graph-index-defaults">
				<tbody>
					<tr>
						<td class="param graph_default_index_backend">Default indexing backend</td>
						<td class="value graph_default_index_backend">
							<xsl:value-of select="indexing/default_indexing_backend/text()"/>
						</td>
					</tr>
				</tbody>
			</table>
		</xsl:if>

		<h2 class="title">Graph relationships</h2>

		<ul class="rel_list">
			<li>
				<a><xsl:attribute name="href"><xsl:copy-of select="concat($vertex_doc_file, '#vertices_toc')"/></xsl:attribute>Vertices</a>
			</li>
			<li>
				<a><xsl:attribute name="href"><xsl:copy-of select="concat($edge_doc_file, '#edges_toc')"/></xsl:attribute>Edges</a>
			</li>
			<li>
				<a><xsl:attribute name="href"><xsl:copy-of select="concat($property_doc_file, '#properties_toc')"/></xsl:attribute>Properties</a>
			</li>
			<li>
				<a><xsl:attribute name="href"><xsl:copy-of select="concat($index_doc_file, '#graph_indexes_toc')"/></xsl:attribute>Graph Indexes</a>
			</li>
			<li>
				<a><xsl:attribute name="href"><xsl:copy-of select="concat($local_property_index_file, '#local_property_indexes_toc')"/></xsl:attribute>Local Property Indexes</a>
			</li>
			<li>
				<a><xsl:attribute name="href"><xsl:copy-of select="concat($local_edge_index_file, '#local_edge_indexes_toc')"/></xsl:attribute>Local Edge Indexes</a>
			</li>
		</ul>
    </xsl:template>

    <xsl:template match="schema-meta[(count(/doc-root/child::*) > 1) and (count(/doc-root/doc-meta[text() = 'allinone']) = 0)]"/>

    <xsl:template match="schema-doctags-meta"/>

    <xsl:template match="doc-meta"/>

<!-- PROPERTIES -->
    <xsl:template match="schema-properties">
    	<a name="properties_toc"/>
    	<xsl:choose>
    		<xsl:when test="$allinone">
    			<h1 class="title allinone">Properties</h1>
    		</xsl:when>
    		<xsl:otherwise>
			    <h1 class="title">Graph schema documentation - &quot;<xsl:value-of select="../schema-meta/name/text()"/>&quot; properties</h1>
			    <a>
			    	<xsl:attribute name="href"><xsl:value-of select="$main_doc_file"/></xsl:attribute>Back to index
			    </a>
	    	</xsl:otherwise>
	    </xsl:choose>
		<xsl:apply-templates select="property">
			<xsl:sort select="key/text()"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="property">
		<a>
			<xsl:attribute name="name">prop_<xsl:value-of select="key/text()"/></xsl:attribute>
		</a>
		<h3 class="prop_title"><xsl:value-of select="key/text()"/></h3>

		<table class="prop_def">
			<tbody>
				<tr>
					<td class="param prop_name">Key</td>
					<td class="value prop_name">
						<xsl:value-of select="key/text()"/>
						<xsl:call-template name="doc-tag-list"/>
					</td>
				</tr>
				<tr>
					<td class="param prop_data_type">Data type</td>
					<td class="value prop_data_type">
						<a>
							<xsl:attribute name="href"><xsl:value-of select="concat($java_api_javadoc_url_base, '/', translate(data_type/text(), '\.', '\/'), '.html')"/></xsl:attribute>
							<xsl:value-of select="data_type/text()"/>
						</a>
					</td>
				</tr>
				<tr>
					<td class="param prop_desc">Description</td>
					<td class="value prop_desc"><xsl:value-of select="description/text()"/></td>
				</tr>
				<tr>
					<td class="param prop_cardinality">Cardinality</td>
					<td class="value prop_cardinality">
						<a>
							<xsl:attribute name="href"><xsl:value-of select="concat($titan_api_javadoc_url_base, '/com/thinkaurelius/titan/core/Cardinality.html', '#', cardinality/text())"/></xsl:attribute>
							<xsl:value-of select="cardinality/text()"/>
						</a>
					</td>
				</tr>
				<xsl:if test="ttl">
				<tr>
					<td class="param prop_ttl">TTL</td>
					<td class="value prop_ttl"><xsl:value-of select="ttl/text()"/></td>
				</tr>
				</xsl:if>
				<xsl:if test="meta/encryption">
				<tr>
					<td class="param prop_encryption">Encryption</td>
					<td class="value prop_encryption">
						<xsl:if test="meta/encryption/always/text()='true'">
						ALWAYS
						<br/>
						</xsl:if>
						<xsl:call-template name="list-types">
							<xsl:with-param name="nodes" select="meta/encryption/only_vertices/*"/>
							<xsl:with-param name="link_prefix">vertices.xml#vertex_</xsl:with-param>
							<xsl:with-param name="list_prefix">Only for vertexes: </xsl:with-param>
						</xsl:call-template>
						<xsl:call-template name="list-types">
							<xsl:with-param name="nodes" select="meta/encryption/only_edges/*"/>
							<xsl:with-param name="link_prefix">edges.xml#edge_</xsl:with-param>
							<xsl:with-param name="list_prefix">Only for edges: </xsl:with-param>
						</xsl:call-template>
					</td>
				</tr>
				</xsl:if>
			</tbody>
		</table>

	</xsl:template>


<!-- VERTICES -->

    <xsl:template match="schema-vertices">
	    <a name="vertices_toc"/>
		<xsl:choose>
    		<xsl:when test="$allinone">
    			<h1 class="title allinone">Vertices</h1>
    		</xsl:when>
    		<xsl:otherwise>
			    <h1 class="title">Graph schema documentation - &quot;<xsl:value-of select="../schema-meta/name/text()"/>&quot; vertices</h1>
			    <a>
			    	<xsl:attribute name="href"><xsl:value-of select="$main_doc_file"/></xsl:attribute>Back to index
			    </a>
		    </xsl:otherwise>
	    </xsl:choose>
		<xsl:apply-templates select="vertex">
			<xsl:sort select="label/text()"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="vertex">
		<a>
			<xsl:attribute name="name">vertex_<xsl:value-of select="label/text()"/></xsl:attribute>
		</a>
		<h3 class="vertex_title"><xsl:value-of select="label/text()"/></h3>

		<table class="vertex_def">
			<tbody>
				<tr>
					<td class="param vertex_label">Label</td>
					<td class="value vertex_label">
						<xsl:value-of select="label/text()"/>
						<xsl:call-template name="doc-tag-list"/>
					</td>
				</tr>
				<tr>
					<td class="param vertex_desc">Description</td>
					<td class="value vertex_desc"><xsl:value-of select="description/text()"/></td>
				</tr>
				<tr>
					<td class="param vertex_partition">Partition</td>
					<td class="value vertex_partition"><xsl:value-of select="partition/text()"/></td>
				</tr>
				<tr>
					<td class="param vertex_static">Static</td>
					<td class="value vertex_static"><xsl:value-of select="static/text()"/></td>
				</tr>
				<xsl:if test="ttl">
				<tr>
					<td class="param vertex_ttl">TTL</td>
					<td class="value vertex_ttl"><xsl:value-of select="ttl/text()"/></td>
				</tr>
				</xsl:if>
				<xsl:if test="count(properties/*) > 0">
				<tr>
					<td class="param vertex_properties">Common properties</td>
					<td class="value vertex_properties">
						<xsl:call-template name="describe-properties">
							<xsl:with-param name="nodes" select="properties/*"/>
							<xsl:with-param name="link_prefix">properties.xml#prop_</xsl:with-param>
						</xsl:call-template>
					</td>
				</tr>
				</xsl:if>
				<xsl:if test="count(relationships/*) > 0">
				<tr>
					<td class="param vertex_relationships">Common relationships</td>
					<td class="value vertex_relationships">
						<xsl:call-template name="describe-vertex-relationships">
							<xsl:with-param name="relationships" select="relationships/*"/>
							<xsl:with-param name="edge_link_prefix">edges.xml#edge_</xsl:with-param>
							<xsl:with-param name="vertex_link_prefix">#vertex_</xsl:with-param>
						</xsl:call-template>
					</td>
				</tr>
				</xsl:if>
			</tbody>
		</table>
	</xsl:template>

<!-- EDGES -->

    <xsl:template match="schema-edges">
		<a name="edges_toc"/>
		<xsl:choose>
    		<xsl:when test="$allinone">
    			<h1 class="title allinone">Edges</h1>
    		</xsl:when>
    		<xsl:otherwise>
			    <h1 class="title">Graph schema documentation - &quot;<xsl:value-of select="../schema-meta/name/text()"/>&quot; edges</h1>
			    <a>
			    	<xsl:attribute name="href"><xsl:value-of select="$main_doc_file"/></xsl:attribute>Back to index
			    </a>
		    </xsl:otherwise>
	    </xsl:choose>
		<xsl:apply-templates select="edge">
			<xsl:sort select="label/text()"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="edge">
		<a>
			<xsl:attribute name="name">edge_<xsl:value-of select="label/text()"/></xsl:attribute>
		</a>
		<h3 class="edge_title"><xsl:value-of select="label/text()"/></h3>

		<table class="edge_def">
			<tbody>
				<tr>
					<td class="param edge_label">Label</td>
					<td class="value edge_label">
						<xsl:value-of select="label/text()"/>
						<xsl:if test="unidirected/text() = 'true' or invisible/text() = 'true'">
							<span class="edge_features"> (
							<xsl:if test="unidirected/text()!='false'">
								<span class="edge_unidir">unidirected</span>
							</xsl:if>
							<xsl:if test="invisible/text()!='false'">
								<span class="edge_invisible">invisible</span>
							</xsl:if>
							)</span>
						</xsl:if>
						<xsl:call-template name="doc-tag-list"/>
					</td>
				</tr>
				<tr>
					<td class="param edge_desc">Description</td>
					<td class="value edge_desc"><xsl:value-of select="description/text()"/></td>
				</tr>
				<tr>
					<td class="param edge_multiplicity">Multiplicity</td>
					<td class="value edge_multiplicity">
						<a>
							<xsl:attribute name="href"><xsl:value-of select="concat($titan_api_javadoc_url_base, '/com/thinkaurelius/titan/core/Multiplicity.html#', multiplicity/text())"/></xsl:attribute>
							<xsl:value-of select="multiplicity/text()"/>
						</a>
					</td>
				</tr>
				<xsl:if test="ttl">
				<tr>
					<td class="param edge_ttl">TTL</td>
					<td class="value edge_ttl"><xsl:value-of select="ttl/text()"/></td>
				</tr>
				</xsl:if>
				<xsl:if test="signature">
				<tr>
					<td class="param edge_signature">Signature</td>
					<td class="value edge_signature">
						<xsl:call-template name="list-types">
							<xsl:with-param name="nodes" select="signature/*"/>
							<xsl:with-param name="link_prefix"><xsl:value-of select="concat($property_doc_file, '#', $property_a_prefix)"/></xsl:with-param>
							<xsl:with-param name="list_prefix"><xsl:text></xsl:text></xsl:with-param>
						</xsl:call-template>
					</td>
				</tr>
				</xsl:if>
				<xsl:if test="sort_key">
				<tr>
					<td class="param edge_sort_key">Sort key</td>
					<td class="value edge_sort_key">
						<xsl:call-template name="list-types">
							<xsl:with-param name="nodes" select="sort_key/keys/*"/>
							<xsl:with-param name="link_prefix"><xsl:value-of select="concat($property_doc_file, '#', $property_a_prefix)"/></xsl:with-param>
							<xsl:with-param name="list_prefix"><xsl:text></xsl:text></xsl:with-param>
						</xsl:call-template>
						(<xsl:value-of select="sort_key/order/text()"/>)
					</td>
				</tr>
				</xsl:if>
				<xsl:if test="count(properties/*) > 0">
				<tr>
					<td class="param edge_properties">Common properties</td>
					<td class="value edge_properties">
						<xsl:call-template name="describe-properties">
							<xsl:with-param name="nodes" select="properties/*"/>
							<xsl:with-param name="link_prefix"><xsl:value-of select="concat($property_doc_file, '#', $property_a_prefix)"/></xsl:with-param>
						</xsl:call-template>
					</td>
				</tr>
				</xsl:if>
				<xsl:if test="count(relationships/*) > 0">
				<tr>
					<td class="param edge_relationships">Relationships</td>
					<td class="value edge_relationships">
						<xsl:for-each select="relationships/*">
							<dl class="edge_relationship">
								<dt class="edge_relationship_vertices">
									<span class="rel_vertex rel_out_vertex">
										<a>
											<xsl:attribute name="href"><xsl:value-of select="concat($vertex_doc_file, '#', $vertex_a_prefix)"/><xsl:value-of select="out/text()"/></xsl:attribute>
											<xsl:value-of select="out/text()"/>
										</a>
									</span>
									<xsl:text disable-output-escaping="yes">&amp;rarr;</xsl:text>
									<span class="rel_out_edge_in">[ <xsl:value-of select="../../label/text()"/> ]</span>
									<xsl:text disable-output-escaping="yes">&amp;rarr;</xsl:text>
									<span class="rel_vertex rel_in_vertex">
										<a>
											<xsl:attribute name="href"><xsl:value-of select="concat($vertex_doc_file, '#', $vertex_a_prefix)"/><xsl:value-of select="in/text()"/></xsl:attribute>
											<xsl:value-of select="in/text()"/>
										</a>
									</span>
									<xsl:call-template name="doc-tag-list"/>
								</dt>
								<dd class="edge_relationship">
									<xsl:value-of select="description/text()"/>
								</dd>
							</dl>
						</xsl:for-each>
					</td>
				</tr>
				</xsl:if>
			</tbody>
		</table>
	</xsl:template>


<!-- GRAPH INDEXES -->

    <xsl:template match="schema-indexes">
    	<a name="graph_indexes_toc"/>
		<xsl:choose>
    		<xsl:when test="$allinone">
    			<h1 class="title allinone">Graph indexes</h1>
    		</xsl:when>
    		<xsl:otherwise>
			    <h1 class="title">Graph schema documentation - &quot;<xsl:value-of select="../schema-meta/name/text()"/>&quot; indexes</h1>
			    <a>
			    	<xsl:attribute name="href"><xsl:value-of select="$main_doc_file"/></xsl:attribute>Back to index
			    </a>
	    	</xsl:otherwise>
	    </xsl:choose>
		<xsl:apply-templates select="index">
			<xsl:sort select="name/text()"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="index">
		<a>
			<xsl:attribute name="name">graph_index_<xsl:value-of select="name/text()"/></xsl:attribute>
		</a>
		<h3 class="graph_index_title"><xsl:value-of select="name/text()"/></h3>

		<table class="graph_index_def">
			<tbody>
				<tr>
					<td class="param graph_index_name">Name</td>
					<td class="value graph_index_name">
						<xsl:value-of select="name/text()"/>
						<xsl:call-template name="doc-tag-list"/>
					</td>
				</tr>
				<tr>
					<td class="param graph_index_reltype">Relationship type</td>
					<td class="value graph_index_reltype">
						<xsl:value-of select="rel_type/text()"/>
					</td>
				</tr>
				<tr>
					<td class="param graph_index_type">Index type</td>
					<td class="value graph_index_type">
						<xsl:value-of select="index_type/text()"/>
					</td>
				</tr>
				<tr>
					<td class="param graph_index_unique">Unique</td>
					<td class="value graph_index_unique">
						<xsl:value-of select="unique/text()"/>
					</td>
				</tr>
				<xsl:if test="index_backend or (index_type/text() = 'mixed')">
				<tr>
					<td class="param graph_index_backend">Index backend</td>
					<td class="value graph_index_backend">
						<xsl:choose>
							<xsl:when test="index_backend">
								<xsl:value-of select="index_backend/text()"/>
							</xsl:when>
							<xsl:when test="/doc-root/schema-meta/indexing/default_indexing_backend">
								<xsl:value-of select="/doc-root/schema-meta/indexing/default_indexing_backend/text()"/> (default)
							</xsl:when>
							<xsl:otherwise>
								UNDEFINED
							</xsl:otherwise>
						</xsl:choose>
					</td>
				</tr>
				</xsl:if>
				<xsl:if test="index_only">
				<tr>
					<td class="param graph_index_only_label">Index only label</td>
					<td class="value graph_index_only_label">
						<a class="graph_index_mapping">
							<xsl:attribute name="href"><xsl:copy-of select="concat($vertex_doc_file, '#', index_only/text())"/></xsl:attribute>
							<xsl:value-of select="index_only/text()"/>
						</a>
					</td>
				</tr>
				</xsl:if>
				<tr>
					<td class="param graph_index_keys">Keys</td>
					<td class="value graph_index_keys">
						<xsl:for-each select="keys/*">
							<a>
								<xsl:attribute name="href"><xsl:value-of select="concat($property_doc_file, '#', $property_a_prefix, key/text())"/></xsl:attribute>
								<span class="graph_index_keys_key">
									<xsl:value-of select="key/text()"/>
								</span>
							</a>
							<span class="graph_index_mapping">
							<xsl:if test="mapping">
							(mapping=
								<a class="graph_index_mapping">
									<xsl:attribute name="href"><xsl:value-of select="concat($titan_api_javadoc_url_base, '/com/thinkaurelius/titan/core/schema/Mapping.html#', mapping/text())"/></xsl:attribute>
									<xsl:value-of select="mapping/text()"/>
								</a>)
							</xsl:if>
							</span>
							<span class="graph_index_params">
							<xsl:if test="count(parameters/*) > 0">
							(parameters:
								<xsl:for-each select="parameters/*">
									<xsl:if test="position() > 1">; </xsl:if>
									<span class="graph_index_param_key"><xsl:value-of select="param_key/text()"/></span>=<span class="graph_index_param_value"><xsl:value-of select="param_value/text()"/></span>
								</xsl:for-each>)
							</xsl:if>
							</span>
						</xsl:for-each>
					</td>
				</tr>
			</tbody>
		</table>
	</xsl:template>

<!-- LOCAL INDEXES -->

    <xsl:template match="schema-local-property-indexes">
    	<a name="local_property_indexes_toc"/>
		<xsl:choose>
    		<xsl:when test="$allinone">
    			<h1 class="title allinone">Local property indexes</h1>
    		</xsl:when>
    		<xsl:otherwise>
			    <h1 class="title">Graph schema documentation - &quot;<xsl:value-of select="../schema-meta/name/text()"/>&quot; local property indexes</h1>
			    <a>
			    	<xsl:attribute name="href"><xsl:value-of select="$main_doc_file"/></xsl:attribute>Back to index
			    </a>
	    	</xsl:otherwise>
	    </xsl:choose>
		<xsl:apply-templates select="property-index">
			<xsl:sort select="name/text()"/>
		</xsl:apply-templates>
	</xsl:template>

    <xsl:template match="schema-local-edge-indexes">
    	<a name="local_edge_indexes_toc"/>
		<xsl:choose>
    		<xsl:when test="$allinone">
    			<h1 class="title allinone">Local edge indexes</h1>
    		</xsl:when>
    		<xsl:otherwise>
	    		<h1 class="title">Graph schema documentation - &quot;<xsl:value-of select="../schema-meta/name/text()"/>&quot; local edge indexes</h1>
			    <a>
			    	<xsl:attribute name="href"><xsl:value-of select="$main_doc_file"/></xsl:attribute>Back to index
			    </a>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:apply-templates select="edge-index">
			<xsl:sort select="name/text()"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="edge-index | property-index">
		<a>
			<xsl:attribute name="name">local_index_<xsl:value-of select="name/text()"/></xsl:attribute>
		</a>
		<h3 class="local_index_title"><xsl:value-of select="name/text()"/></h3>

		<table class="local_index_def">
			<tbody>
				<tr>
					<td class="param local_index_name">Name</td>
					<td class="value local_index_name">
						<xsl:value-of select="name/text()"/>
						<xsl:call-template name="doc-tag-list"/>
					</td>
				</tr>
				<xsl:if test="key">
				<tr>
					<td class="param local_index_key">Key</td>
					<td class="value local_index_key">
						<a class="local_index_key">
							<xsl:attribute name="href"><xsl:value-of select="concat($property_doc_file, '#', key/text())"/></xsl:attribute>
							<xsl:value-of select="key/text()"/>
						</a>
					</td>
				</tr>
				</xsl:if>
				<xsl:if test="Label">
				<tr>
					<td class="param local_index_label">Label</td>
					<td class="value local_index_label">
						<a class="local_index_label">
							<xsl:attribute name="href"><xsl:value-of select="concat($edge_doc_file, '#', label/text())"/></xsl:attribute>
							<xsl:value-of select="label/text()"/>
						</a>
					</td>
				</tr>
				</xsl:if>
				<xsl:if test="direction">
				<tr>
					<td class="param local_index_direction">Direction</td>
					<td class="value local_index_direction">
						<a class="local_index_direction">
							<xsl:attribute name="href"><xsl:value-of select="concat($tinkerpop_api_javadoc_url_base, '/org/apache/tinkerpop/gremlin/structure/Direction.html', '#', direction/text())"/></xsl:attribute>
							<xsl:value-of select="direction/text()"/>
						</a>
					</td>
				</tr>
				</xsl:if>
				<tr>
					<td class="param local_index_key">Sort key</td>
					<td class="value local_index_key">
						<xsl:call-template name="list-types">
							<xsl:with-param name="nodes" select="sort_key/keys/*"/>
							<xsl:with-param name="link_prefix">properties.xml#prop_</xsl:with-param>
							<xsl:with-param name="list_prefix"><xsl:text></xsl:text></xsl:with-param>
						</xsl:call-template>
						(<xsl:value-of select="sort_key/order/text()"/>)
					</td>
				</tr>
			</tbody>
		</table>
	</xsl:template>

<!-- UTILITY TEMPLATES -->

	<xsl:template name="list-types">
		<xsl:param name="nodes"/>
		<xsl:param name="link_prefix"/>
		<xsl:param name="list_prefix"/>

		<xsl:if test="count(*) > 0">
			<span class="reltype_ref_list_prefix">
				<xsl:copy-of select="$list_prefix"/>
			</span>
			<span class="reltype_ref_list">
				<xsl:for-each select="$nodes">
					<xsl:if test="position() > 1"><xsl:text>, </xsl:text></xsl:if>
					<a>
						<xsl:attribute name="href"><xsl:copy-of select="$link_prefix"/><xsl:value-of select="./text()"/></xsl:attribute>
						<span class="reltype_ref">
							<xsl:value-of select="./text()"/>
						</span>
					</a>
				</xsl:for-each>
			</span>
		</xsl:if>
	</xsl:template>

	<xsl:template name="describe-properties">
		<xsl:param name="nodes"/>
		<xsl:param name="link_prefix"/>

		<dl class="common_properties">
			<xsl:for-each select="$nodes">
				<dt class="common_property">
					<a>
						<xsl:attribute name="href"><xsl:value-of select="concat($link_prefix, key/text())"/></xsl:attribute>
						<xsl:value-of select="key/text()"/>
					</a>
					<xsl:call-template name="doc-tag-list"/>
				</dt>
				<dd class="common_property"><xsl:value-of select="description/text()"/></dd>
			</xsl:for-each>
		</dl>
	</xsl:template>

	<xsl:template name="describe-vertex-relationships">
		<xsl:param name="relationships"/>
		<xsl:param name="edge_link_prefix"/>
		<xsl:param name="vertex_link_prefix"/>

		<xsl:for-each select="$relationships">
			<dl class="vertex_relationship">
				<dt class="vertex_relationship_vertices">
					<xsl:if test="direction/text() = 'OUT'">
						<xsl:text disable-output-escaping="yes">&amp;rarr;</xsl:text>
					</xsl:if>
					<xsl:if test="direction/text() = 'IN'">
						<xsl:text disable-output-escaping="yes">&amp;larr;</xsl:text>
					</xsl:if>
					<span class="rel_out_edge_in">[ <a>
						<xsl:attribute name="href"><xsl:value-of select="concat($edge_link_prefix, edge/text())"/></xsl:attribute>
						<xsl:value-of select="edge/text()"/>
					</a> ]</span>
					<xsl:if test="direction/text() = 'OUT'">
						<xsl:text disable-output-escaping="yes">&amp;rarr;</xsl:text>
					</xsl:if>
					<xsl:if test="direction/text() = 'IN'">
						<xsl:text disable-output-escaping="yes">&amp;larr;</xsl:text>
					</xsl:if>
					<span class="rel_vertex rel_in_vertex">
						<a>
							<xsl:attribute name="href"><xsl:value-of select="concat($vertex_link_prefix, vertex/text())"/></xsl:attribute>
							<xsl:value-of select="vertex/text()"/>
						</a>
					</span>
					<xsl:call-template name="doc-tag-list"/>
				</dt>
				<dd class="vertex_relationship">
					<xsl:value-of select="description/text()"/>
				</dd>
			</dl>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="doc-tag-list">
		<xsl:for-each select="doctags/tags/tags">
			<xsl:variable name="tag_name" select="./text()"/>
			<xsl:variable name="tag_def" select="/doc-root/schema-doctags-meta/*[doctag/text()=$tag_name]"/>
 			<xsl:if test="$tag_def">
  				<xsl:call-template name="doc-tag">
					<xsl:with-param name="tag_name" select="$tag_def/doctag/text()"/>
					<xsl:with-param name="tag_text" select="$tag_def/text/text()"/>
					<xsl:with-param name="tag_link" select="$tag_def/url/text()"/>
				</xsl:call-template>
 			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="doc-tag">
		<xsl:param name="tag_name"/>
		<xsl:param name="tag_text"/>
		<xsl:param name="tag_link">#</xsl:param>

 		<span class="doc-tag">
			<xsl:attribute name="title"><xsl:value-of select="$tag_text"/></xsl:attribute>
			<a>
				<xsl:attribute name="href"><xsl:value-of select="$tag_link"/></xsl:attribute>
				<xsl:value-of select="$tag_name"/>
			</a>
		</span>
	</xsl:template>

</xsl:stylesheet>