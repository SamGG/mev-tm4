/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ExperimentUtil.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.algorithm.impl;

import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;

public class ExperimentUtil {
    
    public static float distance(FloatMatrix matrix, int e1, int e2, int function, float factor, boolean absolute) {
	float result = Float.NaN;
	switch (function) {
	    case Algorithm.PEARSON:
		result = pearson(matrix, e1, e2, factor);
		factor *= -1;
		break;
	    case Algorithm.COSINE:
		result = cosine(matrix, e1, e2, factor);
		factor *= -1;
		break;
	    case Algorithm.COVARIANCE:
		result = covariance(matrix, e1, e2, factor);
		factor *= -1;
		break;
	    case Algorithm.EUCLIDEAN:
		result = euclidian(matrix, e1, e2, factor);
		break;
	    case Algorithm.DOTPRODUCT:
		result = dotProduct(matrix, e1, e2, factor);
		factor *= -1;
		break;
	    case Algorithm.PEARSONUNCENTERED:
		result = pearsonUncentered(matrix, e1, e2, factor);
		factor *= -1;
		break;
	    case Algorithm.PEARSONSQARED:
		result = (float)Math.pow(pearsonUncentered(matrix, e1, e2, factor), 2);
		factor *= -1;
		break;
	    case Algorithm.MANHATTAN:
		result = manhattan(matrix, e1, e2, factor);
		break;
	    case Algorithm.SPEARMANRANK:
		result = spearmanRank(matrix, e1, e2, factor);
		factor *= -1;
		break;
	    case Algorithm.KENDALLSTAU:
		result = kendallsTau(matrix, e1, e2, factor);
		factor *= -1;
		break;
	    case Algorithm.MUTUALINFORMATION:
		result = mutualInformation(matrix, e1, e2, factor);
		break;
	    default: {}
	}
	if (absolute) {
	    result = Math.abs(result);
	}
	
	return result * factor;
    }
    
    public static float geneDistance(FloatMatrix matrix, FloatMatrix M, int g1, int g2, int function, float factor, boolean absolute) {
	float result = Float.NaN;
	switch (function) {
	    case Algorithm.PEARSON:
		result = genePearson(matrix, M, g1, g2, factor);
		factor *= -1;
		break;
	    case Algorithm.COSINE:
		result = geneCosine(matrix, M, g1, g2, factor);
		factor *= -1;
		break;
	    case Algorithm.COVARIANCE:
		result = geneCovariance(matrix, M, g1, g2, factor);
		factor *= -1;
		break;
	    case Algorithm.EUCLIDEAN:
		result = geneEuclidianDistance(matrix, M, g1, g2, factor);
		break;
	    case Algorithm.DOTPRODUCT:
		result = geneDotProduct(matrix, M, g1, g2, factor);
		factor *= -1;
		break;
	    case Algorithm.PEARSONUNCENTERED:
		result = genePearsonUncentered(matrix, M, g1, g2, factor);
		factor *= -1;
		break;
	    case Algorithm.PEARSONSQARED:
		result = (float)Math.pow(genePearsonUncentered(matrix, M, g1, g2, factor), 2)*factor;
		factor *= -1;
		break;
	    case Algorithm.MANHATTAN:
		result = geneManhattan(matrix, M, g1, g2, factor);
		break;
	    case Algorithm.SPEARMANRANK:
		result = geneSpearmanRank(matrix, M, g1, g2, factor);
		factor *= -1;
		break;
	    case Algorithm.KENDALLSTAU:
		result = geneKendallsTau(matrix, M, g1, g2, factor);
		factor *= -1;
		break;
	    case Algorithm.MUTUALINFORMATION:
		result = geneMutualInformation(matrix, M, g1, g2, factor);
		break;
	    default: {}
	}
	if (absolute) {
	    result = Math.abs(result);
	}
	return result * factor;
    }
    
    //=================
    public static float genePearsonOld(FloatMatrix matrix, FloatMatrix M, int g1, int g2, float factor) {
	if (M == null) {
	    M = matrix;
	}
	float TINY = Float.MIN_VALUE;
	double xt,yt;
	double sxx=0.0;
	double syy=0.0;
	double sxy=0.0;
	double ax =0.0;
	double ay =0.0;
	int k = matrix.getColumnDimension();
	int n = 0;
	int j;
	for (j=0; j<k; j++) {
	    if ((!Float.isNaN(matrix.get(g1,j))) && (!Float.isNaN(M.get(g2,j)))) {
		ax += matrix.get(g1,j);
		ay += M.get(g2,j);
		n++;
	    }
	}
	ax /= n;
	ay /= n;
	for (j=0; j<k; j++) {
	    if ((!Float.isNaN(matrix.get(g1,j))) && (!Float.isNaN(M.get(g2,j)))) {
		xt=matrix.get(g1,j)-ax;
		yt=M.get(g2,j)-ay;
		sxx+=xt*xt;
		syy+=yt*yt;
		sxy+=xt*yt;
	    }
	}
	return(float)(sxy/(Math.sqrt(sxx*syy)+TINY)*factor);
    }
    
    //=================
    
    public static float genePearson(FloatMatrix matrix, FloatMatrix M, int g1, int g2, float factor) {
	if (M == null) {
	    M = matrix;
	}
	
	float[] arrX = matrix.A[g1];
	float[] arrY = M.A[g2];
	int nArrSize = matrix.getColumnDimension();
	
	double dblXY = 0f;
	double dblX  = 0f;
	double dblXX = 0f;
	double dblY  = 0f;
	double dblYY = 0f;
	
	double v_1, v_2;
	int iValidValCount = 0;
	for (int i=0; i<nArrSize; i++) {
	    v_1 = arrX[i];
	    v_2 = arrY[i];
	    if (Double.isNaN(v_1) || Double.isNaN(v_2)) {
		continue;
	    }
	    iValidValCount++;
	    dblXY += v_1*v_2;
	    dblXX += v_1*v_1;
	    dblYY += v_2*v_2;
	    dblX  += v_1;
	    dblY  += v_2;
	}
	if (iValidValCount == 0)
	    return 0f;
	
	//Allows for a comparison of two 'flat' genes (genes with no variability in their
	// expression values), ie. 0, 0, 0, 0, 0
	boolean nonFlat = false;
	NON_FLAT_CHECK: for (int j = 1; j < nArrSize; j++) {
	    if ((!Float.isNaN(arrX[j])) && (!Float.isNaN(arrY[j]))) {
		if (arrX[j] != arrX[j-1]) {
		    nonFlat = true;
		    break NON_FLAT_CHECK;
		}
		if (arrY[j] != arrY[j-1]) {
		    nonFlat = true;
		    break NON_FLAT_CHECK;
		}
	    }
	}
	
	if (nonFlat == false) {
	    return 1.0f;
	}
	
	
	double dblAvgX = dblX/iValidValCount;
	double dblAvgY = dblY/iValidValCount;
	double dblUpper = dblXY-dblX*dblAvgY-dblAvgX*dblY+dblAvgX*dblAvgY*((double)iValidValCount);
	double p1 = (dblXX-dblAvgX*dblX*2d+dblAvgX*dblAvgX*((double)iValidValCount));
	double p2 = (dblYY-dblAvgY*dblY*2d+dblAvgY*dblAvgY*((double)iValidValCount));
	double dblLower = p1*p2;
	return(float)(dblUpper/(Math.sqrt(dblLower)+Double.MIN_VALUE)*(double)factor);
    }
    
    public static float geneCosine(FloatMatrix matrix, FloatMatrix M, int g1, int g2, float factor) {
	if (M == null) {
	    M = matrix;
	}
	double xt,yt;
	double sxy=0.0;
	double sxx=0.0;
	double syy=0.0;
	double tx=0.0;
	double ty=0.0;
	int k = matrix.getColumnDimension();
	int n = 0;
	int j;
	for (j=0; j<k; j++) {
	    if ((!Float.isNaN(matrix.get(g1, j))) && (!Float.isNaN(M.get(g2, j)))) {
		tx = matrix.get(g1, j);
		ty = M.get(g2, j);
		sxy += tx*ty;
		sxx += tx*tx;
		syy += ty*ty;
		n++;
	    }
	}
	return(float)(sxy/(Math.sqrt(sxx)*Math.sqrt(syy))*factor);
    }
    
    public static float geneCovariance(FloatMatrix matrix, FloatMatrix M, int g1, int g2, float factor) {
	if (M == null) {
	    M=matrix;
	}
	double xt,yt;
	double sxy=0.0;
	double ax=0.0;
	double ay=0.0;
	int k = matrix.getColumnDimension();
	int n = 0;
	int j;
	for (j=0; j<k; j++) {
	    if ((!Float.isNaN(matrix.get(g1, j))) && (!Float.isNaN(M.get(g2, j)))) {
		ax += matrix.get(g1, j);
		ay += M.get(g2, j);
		n++;
	    }
	}
	ax /= n;
	ay /= n;
	for (j=0; j<k; j++) {
	    if ((!Float.isNaN(matrix.get(g1, j))) && (!Float.isNaN(M.get(g2, j)))) {
		xt=matrix.get(g1, j)-ax;
		yt=M.get(g2, j)-ay;
		sxy+=xt*yt;
	    }
	}
	return(float)(sxy/((n-1)*1.0)*factor);
    }
    
    public static float geneEuclidianDistance(FloatMatrix matrix, FloatMatrix M, int g1, int g2, float factor) {
	if (M == null) {
	    M = matrix;
	}
	int k = matrix.getColumnDimension();
	int n = 0;
	double sum = 0.0;
	for (int i=0; i<k; i++) {
	    if ((!Float.isNaN(matrix.get(g1,i))) && (!Float.isNaN(M.get(g2,i)))) {
		sum+=Math.pow((matrix.get(g1,i)-M.get(g2,i)),2);
		n++;
	    }
	}
	return(float)(Math.sqrt(sum)*factor);
    }
    
    public static float geneDotProduct(FloatMatrix matrix, FloatMatrix M, int g1, int g2, float factor) {
	if (M == null) {
	    M = matrix;
	}
	int k=matrix.getColumnDimension();
	int n=0;
	double sum=0.0;
	for (int i=0; i<k; i++) {
	    if ((!Float.isNaN(matrix.get(g1,i))) && (!Float.isNaN(M.get(g2,i)))) {
		sum+=matrix.get(g1,i)*M.get(g2,i);
		n++;
	    }
	}
	return(float)(sum/((double)n)*factor);
    }
    
    public static float genePearsonUncentered(FloatMatrix matrix, FloatMatrix M, int g1, int g2, float factor) {
	if (M == null) {
	    M = matrix;
	}
	float TINY = Float.MIN_VALUE;
	double xt,yt;
	double sxx=0.0;
	double syy=0.0;
	double sxy=0.0;
	double ax =0.0;
	double ay =0.0;
	int k = matrix.getColumnDimension();
	int n = 0;
	int j;
	for (j=0; j<k; j++) {
	    if ((!Float.isNaN(matrix.get(g1,j))) && (!Float.isNaN(M.get(g2,j)))) {
		ax += matrix.get(g1,j);
		ay += M.get(g2,j);
		n++;
	    }
	}
	ax /= n;
	ay /= n;
	for (j=0; j<k; j++) {
	    if ((!Float.isNaN(matrix.get(g1,j))) && (!Float.isNaN(M.get(g2,j)))) {
		xt=matrix.get(g1,j);
		yt=M.get(g2,j);
		sxx+=xt*xt;
		syy+=yt*yt;
		sxy+=xt*yt;
	    }
	}
	return(float)(sxy/(Math.sqrt(sxx*syy)+TINY)*factor);
    }
    
    public static float geneManhattan(FloatMatrix matrix, FloatMatrix M, int g1, int g2, float factor) {
	if (M == null) {
	    M = matrix;
	}
	int j;
	double sum = 0.0;
	int n = matrix.getColumnDimension();
	for (j=0; j<n; j++) {
	    if ((!Float.isNaN(matrix.get(g1, j))) && (!Float.isNaN(M.get(g2, j)))) {
		sum += Math.abs(matrix.get(g1,j)-matrix.get(g2, j));
	    }
	}
	return(float)(sum*factor);
    }
    
    public static float geneSpearmanRank(FloatMatrix matrix, FloatMatrix M, int g1, int g2, float factor) {
	if (M == null) {
	    M = matrix;
	}
	int j;
	double vard,t,sg,sf,fac,en3n,en,df,aved;
	double[] wksp1;
	double[] wksp2;
	double d;
	
	int n = matrix.getColumnDimension();
	wksp1=new double[n];
	wksp2=new double[n];
	for (j=0;j<n;j++) {
	    wksp1[j]=matrix.get(g1,j);
	    wksp2[j]=M.get(g2,j);
	}
	sort2(wksp1,wksp2); // Sort each of the data arrays, and convert the entries to ranks.
	sf=crank(wksp1);
	sort2(wksp2,wksp1);
	sg=crank(wksp2);
	d=0.0;
	for (j=0;j<n;j++)
	    d += Math.pow((wksp1[j]-wksp2[j]),2); // Sum the squared diference of ranks.
	en=n;
	en3n=en*en*en-en;
	aved=en3n/6.0-(sf+sg)/12.0; // Expectation value of D,
	fac=(1.0-sf/en3n)*(1.0-sg/en3n);
	vard=((en-1.0)*en*en*Math.pow((en+1.0),2)/36.0)*fac; // and variance of D give
	
	return(float)((1.0-(6.0/en3n)*(d+(sf+sg)/12.0))/Math.sqrt(fac)*factor); // Rank correlation coe cient,
    }
    
    public static float geneKendallsTau(FloatMatrix matrix, FloatMatrix M, int g1, int g2, float factor) {
	if (M == null) {
	    M = matrix;
	}
	float TINY = Float.MIN_VALUE;
	int n = matrix.getColumnDimension();
	int n2 = 0;
	int n1 = 0;
	int is = 0;
	double svar,aa,a2,a1;
	for (int j=0; j<n-1; j++) {               //Loop over rst member of pair,
	    for (int k=(j+1); k<n; k++) {         //and second member.
		a1=matrix.get(g1,j)-matrix.get(g1,k);
		a2=M.get(g2,j)-M.get(g2,k);
		aa=a1*a2;
		if (aa!=0.0) {                    // Neither array has a tie.
		    ++n1;
		    ++n2;
		    if (aa > 0.0) ++is;
		    else --is;
		} else {                          // One or both arrays have ties.
		    if (a1!=0.0) ++n1;             // An \extra x" event.
		    if (a2!=0.0) ++n2;             // An \extra y" event.
		}
	    }
	}
	return(float)(is/(Math.sqrt((double) n1)*Math.sqrt((double)n2)+TINY)*factor);
    }
    
    public static float geneMutualInformation(FloatMatrix matrix, FloatMatrix M, int g1, int g2, float factor) {
	if (M == null) {
	    M = matrix;
	}
	int n=M.getColumnDimension();
	int NumberOfBins=(int)Math.floor(Math.log(n)/Math.log(2));
	int Values=0;
	for (int i=0; i<n; i++) {
	    if ((!Float.isNaN(matrix.get(g1, i))) && (!Float.isNaN(M.get(g2,i))))
		Values++;
	}
	// lexa: position of arguments are different from mutualInformation method ???
	FloatMatrix Gene1Array = new FloatMatrix(Values, 1);
	FloatMatrix Gene2Array = new FloatMatrix(Values, 1);
	int k=0;
	for (int i=0; i<n; i++) {
	    if ((!Float.isNaN(matrix.get(g1, i))) && (!Float.isNaN(matrix.get(g2,i)))) {
		Gene1Array.set(0,k,matrix.get(g1,i));
		Gene2Array.set(0,k,M.get(g2,i));
		k++;
	    }
	}
	n=Values;
	makeDigitalExperiment(Gene1Array, 0);
	makeDigitalExperiment(Gene2Array, 0);
	double[] P1=new double[NumberOfBins];
	double[] P2=new double[NumberOfBins];
	double[][] P12=new double[NumberOfBins][NumberOfBins];
	for (int i=0; i<n; i++) {
	    P1[(int)Gene1Array.get(0,i)-1]++;
	    P2[(int)Gene2Array.get(0,i)-1]++;
	    P12[(int)Gene1Array.get(0,i)-1][(int)Gene2Array.get(0,i)-1]++;
	}
	for (int i=0; i<P1.length; i++) {
	    P1[i]/=n;
	    P2[i]/=n;
	    for (int j=0; j<P1.length; j++) {
		P12[i][j]/=n;
	    }
	}
	double H1=0;
	double H2=0;
	double H12=0;
	double MI=0;
	for (int i=0; i<P1.length; i++) {
	    if (P1[i]!=0) H1 +=P1[i]*Math.log(P1[i])/Math.log(2);
	    if (P2[i]!=0) H2 +=P2[i]*Math.log(P2[i])/Math.log(2);
	    for (int j=0; j<P2.length; j++) {
		if (P12[i][j]!=0) H12+=P12[i][j]*Math.log(P12[i][j])/Math.log(2);
	    }
	}
	H1=-H1;
	H2=-H2;
	H12=-H12;
	MI=(H1+H2-H12)/Math.max(H1,H2);
	return(float)((1-MI)*factor);
    }
    
    public static float pearson(FloatMatrix matrix, int e1, int e2, float factor) {
	float TINY = Float.MIN_VALUE;
	int n, j, k;
	double xt,yt;
	double sxx=0.0;
	double syy=0.0;
	double sxy=0.0;
	double ax =0.0;
	double ay =0.0;
	k=matrix.getRowDimension();
	n=0;
	for (j=0; j<k; j++) {
	    if ((!Float.isNaN(matrix.get(j,e1))) && (!Float.isNaN(matrix.get(j,e2)))) {
		ax += matrix.get(j,e1);
		ay += matrix.get(j,e2);
		n++;
	    }
	}
	ax /= n;
	ay /= n;
	for (j=0; j<k; j++) {
	    if ((!Float.isNaN(matrix.get(j,e1))) && (!Float.isNaN(matrix.get(j,e2)))) {
		xt=matrix.get(j,e1)-ax;
		yt=matrix.get(j,e2)-ay;
		sxx+=xt*xt;
		syy+=yt*yt;
		sxy+=xt*yt;
	    }
	}
	return(float)(sxy/(Math.sqrt(sxx*syy)+TINY)*factor);
    }
    
    public static float cosine(FloatMatrix matrix, int e1, int e2, float factor) {
	int n, j, k;
	double xt,yt;
	double sxy=0.0;
	double sxx=0.0;
	double syy=0.0;
	double tx=0.0;
	double ty=0.0;
	k=matrix.getRowDimension();
	for (j=0; j<k; j++) {
	    if ((!Float.isNaN(matrix.get(j,e1))) && (!Float.isNaN(matrix.get(j,e2)))) {
		tx=matrix.get(j,e1);
		ty=matrix.get(j,e2);
		sxy+=tx*ty;
		sxx+=tx*tx;
		syy+=ty*ty;
	    }
	}
	return(float)(sxy/(Math.sqrt(sxx)*Math.sqrt(syy))*factor);
    }
    
    public static float covariance(FloatMatrix matrix, int e1, int e2, float factor) {
	int n, j, k;
	double xt,yt;
	double sxy=0.0;
	double ax=0.0;
	double ay=0.0;
	k=matrix.getRowDimension();
	n=0;
	for (j=0; j<k; j++) {
	    if ((!Float.isNaN(matrix.get(j,e1))) && (!Float.isNaN(matrix.get(j,e2)))) {
		ax += matrix.get(j,e1);
		ay += matrix.get(j,e2);
		n++;
	    }
	}
	ax /= n;
	ay /= n;
	for (j=0; j<k; j++) {
	    if ((!Float.isNaN(matrix.get(j,e1))) && (!Float.isNaN(matrix.get(j,e2)))) {
		xt=matrix.get(j,e1)-ax;
		yt=matrix.get(j,e2)-ay;
		sxy+=xt*yt;
	    }
	}
	return(float)(sxy/((n-1)*1.0f)*factor);
    }
    
    public static float euclidian(FloatMatrix matrix, int e1, int e2, float factor) {
	int n=matrix.getRowDimension();
	double sum=0.0;
	for (int i=0; i<n; i++) {
	    if ((!Float.isNaN(matrix.get(i,e1))) && (!Float.isNaN(matrix.get(i,e2)))) {
		sum+=Math.pow((matrix.get(i,e1)-matrix.get(i,e2)),2);
	    }
	}
	return(float)(Math.sqrt(sum)*factor);
    }
    
    public static float dotProduct(FloatMatrix matrix, int e1, int e2, float factor) {
	int k=matrix.getRowDimension();
	double sum=0.0;
	int n=0;
	for (int i=0; i<k; i++) {
	    if ((!Float.isNaN(matrix.get(i,e1))) && (!Float.isNaN(matrix.get(i,e2)))) {
		sum+=matrix.get(i,e1)*matrix.get(i,e2);
		n++;
	    }
	}
	return(float)(sum/((double)n)*factor);
    }
    
    public static float pearsonUncentered(FloatMatrix matrix, int e1, int e2, float factor) {
	float TINY = Float.MIN_VALUE;
	int n, j, k;
	double xt,yt;
	double sxx=0.0;
	double syy=0.0;
	double sxy=0.0;
	double ax =0.0;
	double ay =0.0;
	k=matrix.getRowDimension();
	n=0;
	for (j=0; j<k; j++) {
	    if ((!Float.isNaN(matrix.get(j,e1))) && (!Float.isNaN(matrix.get(j,e2)))) {
		ax += matrix.get(j,e1);
		ay += matrix.get(j,e2);
		n++;
	    }
	}
	ax /= n;
	ay /= n;
	for (j=0; j<k; j++) {
	    if ((!Float.isNaN(matrix.get(j,e1))) && (!Float.isNaN(matrix.get(j,e2)))) {
		xt=matrix.get(j,e1);
		yt=matrix.get(j,e2);
		sxx+=xt*xt;
		syy+=yt*yt;
		sxy+=xt*yt;
	    }
	}
	return(float)(sxy/(Math.sqrt(sxx*syy)+TINY)*factor);
    }
    
    public static float manhattan(FloatMatrix matrix, int e1, int e2, float factor) {
	int n, j;
	double sum=0.0;
	n=matrix.getRowDimension();
	for (j=0; j<n; j++) {
	    if ((!Float.isNaN(matrix.get(j,e1))) && (!Float.isNaN(matrix.get(j,e2)))) {
		sum+=Math.abs(matrix.get(j,e1)-matrix.get(j,e2));
	    }
	}
	return(float)(sum*factor);
    }
    
    public static float spearmanRank(FloatMatrix matrix, int e1, int e2, float factor) {
	int j;
	double vard,t,sg,sf,fac,en3n,en,df,aved;
	double[] wksp1;
	double[] wksp2;
	double d;
	
	int n=matrix.getRowDimension();
	wksp1=new double[n];
	wksp2=new double[n];
	for (j=0;j<n;j++) {
	    wksp1[j]=matrix.get(j,e1);
	    wksp2[j]=matrix.get(j,e2);
	}
	sort2(wksp1,wksp2); // Sort each of the data arrays, and convert the entries to ranks.
	sf=crank(wksp1);
	sort2(wksp2,wksp1);
	sg=crank(wksp2);
	d=0.0;
	for (j=0;j<n;j++) d += Math.pow((wksp1[j]-wksp2[j]),2); // Sum the squared diference of ranks.
	en=n;
	en3n=en*en*en-en;
	aved=en3n/6.0-(sf+sg)/12.0; // Expectation value of D,
	fac=(1.0-sf/en3n)*(1.0-sg/en3n);
	vard=((en-1.0)*en*en*Math.pow((en+1.0),2)/36.0)*fac; // and variance of D give
	
	return(float)((1.0-(6.0/en3n)*(d+(sf+sg)/12.0))/Math.sqrt(fac)*factor); // Rank correlation coecient,
    }
    
    public static float kendallsTau(FloatMatrix matrix, int e1, int e2, float factor) {
	float TINY=Float.MIN_VALUE;
	int n=matrix.getRowDimension();
	int n2=0;
	int n1=0;
	int is=0;
	double svar,aa,a2,a1;
	for (int j=0; j<n-1; j++) {               //Loop over rst member of pair,
	    for (int k=(j+1); k<n; k++) {         //and second member.
		a1=matrix.get(j,e1)-matrix.get(k,e1);
		a2=matrix.get(j,e2)-matrix.get(k,e2);
		aa=a1*a2;
		if (aa!=0.0) {                    // Neither array has a tie.
		    ++n1;
		    ++n2;
		    if (aa > 0.0) ++is;
		    else --is;
		} else {                          // One or both arrays have ties.
		    if (a1!=0.0) ++n1;             // An \extra x" event.
		    if (a2!=0.0) ++n2;             // An \extra y" event.
		}
	    }
	}
	return(float)(is/(Math.sqrt((double) n1)*Math.sqrt((double)n2)+TINY)*factor);
    }
    
    public static float mutualInformation(FloatMatrix matrix, int e1, int e2, float factor) {
	int n=matrix.getRowDimension();
	int NumberOfBins=(int)Math.floor(Math.log(n)/Math.log(2));
	int Values=0;
	for (int i=0; i<n; i++) {
	    if ((!Float.isNaN(matrix.get(i,e1))) && (!Float.isNaN(matrix.get(i,e2)))) Values++;
	}
	
	FloatMatrix Experiment1Array = new FloatMatrix(1, Values);
	FloatMatrix Experiment2Array = new FloatMatrix(1, Values);
	int k=0;
	for (int i=0; i<n; i++) {
	    if ((!Float.isNaN(matrix.get(i,e1))) && (!Float.isNaN(matrix.get(i,e2)))) {
		Experiment1Array.set(k,0,matrix.get(i,e1));
		Experiment2Array.set(k,0,matrix.get(i,e2));
		k++;
	    }
	}
	n=Values;
	makeDigitalExperiment(Experiment1Array, 0);
	makeDigitalExperiment(Experiment2Array, 0);
	double[] P1=new double[NumberOfBins];
	double[] P2=new double[NumberOfBins];
	double[][] P12=new double[NumberOfBins][NumberOfBins];
	for (int i=0; i<n; i++) {
	    P1[(int)Experiment1Array.get(i,0)-1]++;
	    P2[(int)Experiment2Array.get(i,0)-1]++;
	    P12[(int)Experiment1Array.get(i,0)-1][(int)Experiment2Array.get(i,0)-1]++;
	}
	for (int i=0; i<P1.length; i++) {
	    P1[i]/=n;
	    P2[i]/=n;
	    for (int j=0; j<P1.length; j++) {
		P12[i][j]/=n;
	    }
	}
	double H1=0;
	double H2=0;
	double H12=0;
	double MI=0;
	for (int i=0; i<P1.length; i++) {
	    if (P1[i]!=0) H1 +=P1[i]*Math.log(P1[i])/Math.log(2);
	    if (P2[i]!=0) H2 +=P2[i]*Math.log(P2[i])/Math.log(2);
	    for (int j=0; j<P2.length; j++) {
		if (P12[i][j]!=0) H12+=P12[i][j]*Math.log(P12[i][j])/Math.log(2);
	    }
	}
	H1=-H1;
	H2=-H2;
	H12=-H12;
	if (Math.max(H1,H2)!=0) {
	    MI=(H1+H2-H12)/Math.max(H1,H2);
	} else {
	    MI=(H1+H2-H12);
	}
	return(float)((1-MI)*factor);
    }
    
    public static void makeDigitalExperiment(FloatMatrix matrix, int e) {
	int n=matrix.getRowDimension();
	int NumberOfBins=(int)Math.floor(Math.log(n)/Math.log(2));
	int Step=1000000/NumberOfBins;
	float Minimum=Float.MAX_VALUE;
	float Maximum=0;
	for (int i=0; i<n; i++) {
	    if (matrix.get(i,e)<Minimum) Minimum=matrix.get(i,e);
	}
	for (int i=0; i<n; i++) {
	    matrix.set(i,e,matrix.get(i,e)-Minimum);
	}
	for (int i=0; i<n; i++) {
	    if (matrix.get(i,e)>Maximum) Maximum=matrix.get(i,e);
	}
	if (Maximum!=0) {
	    for (int i=0; i<n; i++) {
		matrix.set(i,e,matrix.get(i,e)/Maximum);
	    }
	}
	for (int i=0; i<n; i++) {
	    if (matrix.get(i,e)==1.0) {
		matrix.set(i,e,(float)NumberOfBins);
	    } else {
		matrix.set(i,e,(float)(Math.floor(matrix.get(i,e)*1000000/Step)+1));
	    }
	}
    }
    
    /** Given a sorted array w[0..n-1], replaces the elements by their rank,
     *  including midranking of ties, and returns as s the sum of f3 ? f,
     *  where fis the number of elements in each tie.
     */
    public static double crank(double w[]) {
	int j=0,ji,jt;
	double t,rank;
	double s=0.0;
	int n=w.length;
	while (j < n-1) {
	    if (w[j+1] != w[j]) { // Not a tie.
		w[j]=j;
		++j;
	    } else { // A tie:
		for (jt=j+1; jt<n && w[jt]==w[j]; jt++); // How far does it go?
		rank=0.5*(j+jt-1);                       // This is the mean rank of the tie,
		for (ji=j;ji<=(jt-1);ji++) w[ji]=rank;   // so enter it into all the tied entries,
		t=jt-j;
		s += t*t*t-t;                            // and update s.
		j=jt;
	    }
	}
	if (j == n-1) w[n-1]=n-1; // If the last element was not tied, this is its rank.
	return s;
    }
    
    /**
     * Sorts an array arr[1..n] into ascending order using Quicksort,
     * while making the corresponding rearrangement of the array brr[1..n].
     */
    public static void sort2(double[] arr, double[] brr) {
	int n=arr.length;
	int i,ir=n-1,j,k,l=0;
	int[] istack;
	int jstack=0;
	double a,b,temp;
	double dummy;
	istack=new int[50];
	for (;;) {
	    if (ir-l < 7) {
		for (j=l+1;j<=ir;j++) {
		    a=arr[j];
		    b=brr[j];
		    for (i=j-1;i>=l;i--) {
			if (arr[i] <= a) break;
			arr[i+1]=arr[i];
			brr[i+1]=brr[i];
		    }
		    arr[i+1]=a;
		    brr[i+1]=b;
		}
		if (jstack==0) {
		    istack=null;
		    return;
		}
		ir=istack[jstack];
		l=istack[jstack-1];
		jstack -= 2;
	    } else {
		k=(l+ir) >> 1;
		dummy=arr[k];
		arr[k]=arr[l+1];
		arr[l+1]=arr[k];
		
		dummy=brr[k];
		brr[k]=brr[l+1];
		brr[l+1]=brr[k];
		
		if (arr[l] > arr[ir]) {
		    dummy=arr[l];
		    arr[l]=arr[ir];
		    arr[ir]=dummy;
		    
		    dummy=brr[l];
		    brr[l]=brr[ir];
		    brr[ir]=dummy;
		    
		}
		if (arr[l+1] > arr[ir]) {
		    dummy=arr[l+1];
		    arr[l+1]=arr[ir];
		    arr[ir]=dummy;
		    
		    dummy=brr[l+1];
		    brr[l+1]=brr[ir];
		    brr[ir]=dummy;
		}
		if (arr[l] > arr[l+1]) {
		    dummy=arr[l];
		    arr[l]=arr[l+1];
		    arr[l+1]=dummy;
		    
		    dummy=brr[l];
		    brr[l]=brr[l+1];
		    brr[l+1]=dummy;
		}
		i=l+1; // Initialize pointers for partitioning.
		j=ir;
		a=arr[l+1]; //Partitioning element.
		b=brr[l+1];
		for (;;) { // Beginning of innermost loop.
		    do i++; while (arr[i] < a);
		    do j--; while (arr[j] > a);
		    if (j < i) break; // Pointers crossed. Partitioning complete.
		    dummy=arr[i];
		    arr[i]=arr[j];
		    arr[j]=dummy;
		    dummy=brr[i];
		    brr[i]=brr[j];
		    brr[j]=dummy;
		} // End of innermost loop.
		arr[l+1]=arr[j]; // Insert partitioning element in both arrays.
		arr[j]=a;
		brr[l+1]=brr[j];
		brr[j]=b;
		jstack += 2;
		if (jstack > 50) System.out.println("NSTACK too small in sort2.");
		if (ir-i+1 >= j-l) {
		    istack[jstack]=ir;
		    istack[jstack-1]=i;
		    ir=j-1;
		} else {
		    istack[jstack]=j-1;
		    istack[jstack-1]=l;
		    l=i;
		}
	    }
	}
    }
}
